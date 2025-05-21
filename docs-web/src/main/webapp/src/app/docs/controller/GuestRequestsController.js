angular.module('teedyApp')
    .controller('GuestRequestsController', function($scope, $http) {
        $scope.requests = [];

        function load() {
            $http.get('/api/guest-requests').then(resp => {
                $scope.requests = resp.data;
            });
        }
        load();

        $scope.decide = function(req, accept) {
            $http.post(`/api/guest-requests/${req.id}/decision?accept=${accept}`)
                .then(() => load());
        };
    });