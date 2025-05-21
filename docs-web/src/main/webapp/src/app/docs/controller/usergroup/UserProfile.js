'use strict';

angular.module('docs').controller('UserProfile', function($stateParams, Restangular, $scope, $timeout) {
  $scope.messages = [];
  $scope.chatMessage = '';
  let socket = null;
  let reconnectAttempts = 0;
  const maxReconnectAttempts = 5;
  const reconnectInterval = 3000;
  let isManuallyClosed = false;
  let userLoaded = false; // 新增：标记用户是否已加载

  function scrollToBottom() {
    $timeout(() => {
      const chatBox = document.getElementById('chatBox');
      if (chatBox) {
        chatBox.scrollTop = chatBox.scrollHeight;
      }
    }, 100);
  }

  function connectWebSocket() {
    if (!userLoaded || !($scope.user && $scope.user.username)) {
      //console.warn("用户信息未加载，暂不连接 WebSocket");
      return;
    }

    if (socket && socket.readyState === WebSocket.OPEN) {
      console.log('WebSocket 已连接，无需重复连接');
      return;
    }

    isManuallyClosed = false;

    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
    const host = window.location.host;
    const wsUrl = `${protocol}://${host}/docs-web/ws/chat?from=${$scope.userInfo.username}&to=${$scope.user.username}`;

    socket = new WebSocket(wsUrl);

    socket.onopen = function () {
      console.log("WebSocket 已连接");
      reconnectAttempts = 0;

      const historyRequest = {
        type: 'history',
        from: $scope.userInfo.username,
        to: $scope.user.username
      };
      socket.send(JSON.stringify(historyRequest));
    };

    socket.onmessage = function (event) {
      const msg = JSON.parse(event.data);
      $scope.$apply(() => {
        if (msg.type === 'history') {
          $scope.messages = msg.messages || [];
        } else {
          $scope.messages.push(msg);
        }
        scrollToBottom();
      });
    };

    socket.onclose = function () {
      console.log("WebSocket 连接关闭");
      if (!isManuallyClosed && reconnectAttempts < maxReconnectAttempts) {
        reconnectAttempts++;
        console.log(`尝试第 ${reconnectAttempts} 次重连...`);
        $timeout(connectWebSocket, reconnectInterval);
      } else if (isManuallyClosed) {
        console.log("手动关闭，不执行重连");
      } else {
        console.warn("达到最大重连次数，停止重连");
      }
    };

    socket.onerror = function (err) {
      console.error("WebSocket 错误:", err);
    };
  }

  // 加载用户信息
  Restangular.one('user', $stateParams.username).get().then(function(data) {
    $scope.user = data;
    userLoaded = true;

    console.log("用户已就绪，创建 WebSocket 连接");
    console.log($scope.userInfo.username,"---",$scope.user.username);
    if($scope.userInfo.username != $scope.user.username){
      connectWebSocket();
    }

    $scope.sendMessage = function () {
      if (!$scope.chatMessage || $scope.chatMessage.trim() === '') return;

      const msg = {
        from: $scope.userInfo.username,
        to: $scope.user.username,
        content: $scope.chatMessage
      };

      if (socket && socket.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify(msg));
        $scope.messages.push(angular.copy(msg));
        $scope.chatMessage = '';
        scrollToBottom();
      } else {
        alert('用户连接中，请稍后在点击发送！');
      }
    };

    $scope.$on('$destroy', function () {
      if (socket) {
        isManuallyClosed = true;
        socket.close();
      }
    });
  });
});
