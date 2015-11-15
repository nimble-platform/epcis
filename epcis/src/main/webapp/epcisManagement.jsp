<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="css/bootstrap.min.css" rel="stylesheet" media="screen">
<link href="css/bootstrap-switch.min.css" rel="stylesheet"
	media="screen">
<link href="css/bootstrap-select.min.css" rel="stylesheet"
	media="screen">
</head>
<body>
	<script src="js/jquery.min.js"></script>
	<script src="js/bootstrap.min.js"></script>
	<script src="https://apis.google.com/js/plusone.js"
		type="text/javascript"></script>
	<script src="js/bootstrap-switch.min.js"></script>
	<script src="js/bootstrap-select.js"></script>

	<!-- Visualization: http://bl.ocks.org/mbostock/1153292 -->

	<!-- For Facebook Integration -->
	<script>
		// This is called with the results from from FB.getLoginStatus().
		function statusChangeCallback(response) {
			console.log('statusChangeCallback');
			console.log(response);
			// The response object is returned with a status field that lets the
			// app know the current login status of the person.
			// Full docs on the response object can be found in the documentation
			// for FB.getLoginStatus().
			if (response.status === 'connected') {
				// Logged into your app and Facebook.
				getFacebookInformation();
				document.getElementById("fAccessToken").innerHTML = response.authResponse.accessToken;
				$("#scButton").prop("disabled", false);
			} else if (response.status === 'not_authorized') {
				// The person is logged into Facebook, but not your app.
				console.log("Please Log into this App");
				$("#scButton").prop("disabled", true);
				document.getElementById('consoleMsg').innerHTML = "Need Facebook Login";
				document.getElementById('position').innerHTML = "None";
			} else {
				// The person is not logged into Facebook, so we're not sure if
				// they are logged into this app or not.
				console.log("Please Log into this App");
				$("#scButton").prop("disabled", true);
				document.getElementById('consoleMsg').innerHTML = "Need Facebook Login";
				document.getElementById('position').innerHTML = "None";
			}
		}

		// This function is called when someone finishes with the Login
		// Button.  See the onlogin handler attached to it in the sample
		// code below.
		function checkLoginState() {
			FB.getLoginStatus(function(response) {
				statusChangeCallback(response);
			});
		}

		window.fbAsyncInit = function() {
			FB.init({
				appId : '877904738946867',
				cookie : true, // enable cookies to allow the server to access 
				// the session
				xfbml : true, // parse social plugins on this page
				version : 'v2.4' // use version 2.2
			});

			// Now that we've initialized the JavaScript SDK, we call 
			// FB.getLoginStatus().  This function gets the state of the
			// person visiting this page and can return one of three states to
			// the callback you provide.  They can be:
			//
			// 1. Logged into your app ('connected')
			// 2. Logged into Facebook, but not your app ('not_authorized')
			// 3. Not logged into Facebook and can't tell if they are logged into
			//    your app or not.
			//
			// These three cases are handled in the callback function.

			FB.getLoginStatus(function(response) {

				statusChangeCallback(response);

			});


			$("#scButton").prop("disabled", true);

			// Add Event Listener
			$("#scButton").on(
					'click',
					function(event) {
						var fid = $('#fid').text();
						var fAccessToken = $('#fAccessToken').text();
						var accessModifier = $('#EaccessModifier').val();
						$('#scMsg').val(fid + " : " + fAccessToken);
						$.get("/epcis/Service/SecuredChannel?accessToken="
								+ fAccessToken + "&fid=" + fid
								+ "&accessModifier=" + accessModifier,
								function(data) {
									$('#scMsg').val(data);
								});
					});
		};

		// Load the SDK asynchronously
		(function(d, s, id) {
			var js, fjs = d.getElementsByTagName(s)[0];
			if (d.getElementById(id))
				return;
			js = d.createElement(s);
			js.id = id;
			js.src = "//connect.facebook.net/en_US/sdk.js";
			fjs.parentNode.insertBefore(js, fjs);
		}(document, 'script', 'facebook-jssdk'));

		// Here we run a very simple test of the Graph API after login is
		// successful.  See statusChangeCallback() for when this call is made.
		function getFacebookInformation() {
			console.log('Welcome!  Fetching your information.... ');
			FB.api('/me?fields=name,id,email', function(response) {
				console.log('Successful login for: ' + response.name);
				document.getElementById('fEmail').innerHTML = response.email;
				document.getElementById('fid').innerHTML = response.id;
				getPosition();
			});
		}

		function getPosition() {
			fid = $('#fid').text();
			accessToken = document.getElementById('fAccessToken').innerHTML;
			adminFacebookID = document.getElementById('adminFacebookID').innerHTML;
			ownershipPolicy = document.getElementById('ownershipPolicy').innerHTML;
			if (fid == adminFacebookID) {
				document.getElementById('consoleMsg').innerHTML = "Hello Administrator<br>Ownership Policy: "
						+ ownershipPolicy;
				document.getElementById('position').innerHTML = "Admin";

			} else {
				// If not, check whether Admin is a friend.
				var query = '/me/friends';
				FB
						.api(
								query,
								function(response) {
									if (response && !response.error) {
										isFriendOfAdmin = false;
										$.each(response.data, function(index,
												friend) {
											if (friend.id == adminFacebookID) {
												isFriendOfAdmin = true;
											}
										});
										if (isFriendOfAdmin == true) {
											document
													.getElementById('consoleMsg').innerHTML = "Hello Administrator's friend<br>Ownership Policy: "
													+ ownershipPolicy;
											document.getElementById('position').innerHTML = "Friend";
										} else {
											document
													.getElementById('consoleMsg').innerHTML = "Hello Visitor";
											document.getElementById('position').innerHTML = "Visitor";
										}
									}
								});
			}
		}
	</script>

	<div class="panel panel-info">
		<div class="panel-heading">
			<h3 class="panel-title">EPCIS Access Control Management Tool</h3>
		</div>
		<div class="panel-body">
			<fb:login-button
				data-scope="public_profile,user_friends,user_about_me,user_events,user_likes,user_location,user_posts,user_relationships,user_relationship_details,email"
				onlogin="checkLoginState();" auto_logout_link="true">
			</fb:login-button>
			<a href="#"
					class="list-group-item"><div id="consoleMsg"></div> 
			</a>
			<div class="list-group">
				<a href="#" class="list-group-item active"> Secured Event
					Channel </a>
				<div id="fEmail" hidden="true"></div>
				<div id="fAccessToken" hidden="true"></div>
				<div id="fid" hidden="true"></div>
				<div id="position" hidden="true"></div>

				<a href="#" class="list-group-item"> 
					<button id="scButton" type="submit" class="btn btn-info">Click
						to Enable</button> <input id="scMsg" size="100">
				</a>

			</div>
		</div>
	</div>
</body>
</html>
