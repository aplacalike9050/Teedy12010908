'use strict';

/**
 * User/group controller.
 */
angular.module('docs').controller('UserGroup', function(Restangular, $scope, $state) {
  function lodadUsers() {
    // Load users
    Restangular.one('user/list').get({
      sort_column: 1,
      asc: true
    }).then(function(data) {
      $scope.users = data.users;
    });
  }

  $scope.pendingUsers = [];
  $scope.selectedUser = {};

  $scope.refreshPendingUsers = function() {
    Restangular.one('user/pendingUsers').get().then(function(data) {
      $scope.pendingUsers = data.users;
    });
  };

  // approveUser
  $scope.approveUser = function() {
    Restangular.one('user').post('approveUser', {
      username: $scope.selectedUser.username
    }).then(function(data) {
      closeModal();
      alert("操作成功！");
      lodadUsers();
      $scope.refreshPendingUsers();
    });
  };

  function closeModal() {
    document.getElementById('reviewModal').style.display = 'none';
    document.getElementById('reviewModal').style.opacity = 0;
    $scope.selectedUser = {};
  }

  $scope.closeModalBtn = function() {
    closeModal();
  };

  $scope.rejectUser = function() {
    Restangular.one('user').post('rejectUser', {
      username: $scope.selectedUser.username
    }).then(function(data) {
      closeModal();
      alert("操作成功！");
      $scope.refreshPendingUsers();
    });
  };

  $scope.reviewUser = function(user) {
    $scope.selectedUser = angular.copy(user);
    document.getElementById('reviewModal').style.display = 'block';
    document.getElementById('reviewModal').style.opacity = 1;
  };

  // 加载用户和组
  lodadUsers();

  Restangular.one('group').get({
    sort_column: 1,
    asc: true
  }).then(function(data) {
    $scope.groups = data.groups;
  });

  $scope.openUser = function(user) {
    $state.go('user.profile', { username: user.username });
  };

  $scope.openGroup = function(group) {
    $state.go('group.profile', { name: group.name });
  };

  // 监听 userInfo 加载完成后再刷新 pendingUsers
  $scope.$watch('userInfo', function(newVal) {
    if (newVal && newVal.username === 'admin') {
      $scope.refreshPendingUsers();
    }
  });
});
