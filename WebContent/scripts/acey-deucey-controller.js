var aceydeucey = angular
		.module('aceydeucey', [ 'angular-websocket' ])
		.controller(
				'ctrlr',
				[
						'$scope',
						'$websocket',
						'$filter',
						function($scope, $websocket, $filter) {

							/* Globals */
							$scope.selected_game = {
								id : undefined
							};
							$scope.games = undefined;
							$scope.player = undefined;
							$scope.game_id = undefined;
							$scope.game_name = undefined;
							$scope.game_in_lobby = false;
							$scope.players_in_lobby = undefined;
							$scope.player_is_host = undefined;
							$scope.options = undefined;
							$scope.making_choice = false;
							$scope.high_or_low = false;
							
							$scope.card1 = undefined;
							$scope.card2 = undefined;
							$scope.playerCard = undefined;
							
							
							$scope.msgs = [ "The game is started! " ];
							$scope.is_turn = false;
							$scope.making_accusation = false;
							$scope.pot = undefined;
							
							
							
							var pos = [];
						
							/* Define WebSocket for Communication with Game */
							var ws = $websocket('ws://localhost:8080/AceyDeucey/socket');
							ws
									.onMessage(function(event) {
										$scope.test = event.data;

										var data = JSON.parse(event.data);
										if (data.type == "SETUP") {
											$scope.games = data.games;
											console.log($scope.games);

										} else if (data.type == "UPDATE_POT") {

											$scope.pot = data.pot
											$scope.game_in_lobby = false;
											

										} else if (data.type == "UPDATE_DEBT") {
											$scope.debt = data.debt;
											$scope.net = data.net;
											console.log(data.net);

										} else if (data.type == "CARDS") {
											$scope.card1 = data.card1;
											$scope.card2 = data.card2;
											$scope.playerCard = data.playerCard;

										} else if (data.type == "LOBBY") {
											$scope.game_id = data.gameId;
											$scope.game_name = data.gameName;
											$scope.players_in_lobby = data.players;
											if ($scope.player_is_host == undefined) {
												$scope.player_is_host = data.isHost;
											}
											$scope.game_in_lobby = true;

										} else if (data.type == "TURN") {
											
											$scope.is_turn = true;
											$scope.making_choice = true;
									
										} else if (data.type == "CHECK_ACE") {
											$scope.is_turn = true;
											$scope.high_or_low =true;

								
										} else if (data.type == "MSG") {
											var date = new Date();
											var timestamp = $filter('date')(
													date, "hh:mm:ss");
											var subject = data.subject
											if (subject.length > 0) {
												$scope.msgs.push("("
														+ timestamp + ")"
														+ subject
														+ " " + data.msg);
											} else {
												$scope.msgs.push(data.msg);
											}

										} else if (data.type = "ENDGAME") {
											
											
											//$scope.game_id = undefined

											//

										} else {

										}

									});

							ws.onError(function(event) {
								console.log('connection Error', event);
							});

							ws.onClose(function(event) {
								console.log('connection closed', event);
							});

							$scope.submit = function(input_text) {
								ws.send(input_text);
								console.log("Sending Message");
							}

							/* On load, query for available games */
							var setUp = {
								type : "GET_SETUP"
							}
							ws.send(setUp);

							$scope.createGame = function(game_name, player_name) {
								var newGame = {
									type : "CREATE",
									game : game_name,
									player : player_name
								}
								$scope.player = player_name;
								console.log(newGame);
								ws.send(newGame);

							}
						

							$scope.joinGame = function(player_name) {
								var joinedGame = {
									type : "JOIN",
									game : $scope.selected_game.id,
									player : player_name
								}
								$scope.player = player_name
								ws.send(joinedGame)

							}

							$scope.startGame = function() {
								var start = {
									type : "START",
									game : $scope.game_id
								}
								ws.send(start);
							}

							
							$scope.pass = function() {
								// send move back to server
								var turn = null;

								$scope.is_turn = false;
								$scope.making_choice = false;
								var pass = {
										type: "PASS",
										game:  $scope.game_id
								}
								ws.send(pass);
							}

							$scope.setAceHigh = function(isHigh) {
								var aceChoice = {
										type : "ACE_CHOICE",
										game: $scope.game_id,
										isHigh: isHigh
									
								}
							$scope.high_or_low = false;
								ws.send(aceChoice);
							}

							$scope.play = function(bet) {
								console.log("HERE");
								var playBet = {
									type : "PLAY",
									game : $scope.game_id,
									bet : bet
								}
								$scope.is_turn = false;
								console.log(playBet)
								ws.send(playBet);
							}

								
							

						}
						
						]).filter('reverse', function() {
			return function(items) {
				return items.slice().reverse();
			};
		});
;
