<%@ include file="/WEB-INF/pages/includes.jsp" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <meta name="description" content="">
    <meta name="author" content="">
    <link rel="icon" href="../../favicon.ico">

    <title>Carousel Template for Bootstrap</title>

    <!-- Bootstrap core CSS -->
    <link href="../../resources/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Custom styles for this template -->
    <link href="../../resources/docs/examples/carousel/carousel.css" rel="stylesheet">
</head>
<!-- NAVBAR
================================================== -->
<body>
<div class="row-margin">
    <div class="navbar-wrapper">
        <div class="container">

            <nav class="navbar navbar-inverse navbar-static-top">
                <div class="container">
                    <div class="navbar-header">
                        <button type="button" class="navbar-toggle collapsed" data-toggle="collapse"
                                data-target="#navbar" aria-expanded="false" aria-controls="navbar">
                            <span class="sr-only">Toggle navigation</span>
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                        </button>
                        <a class="navbar-brand" href="#">Project name</a>
                    </div>
                    <div id="navbar" class="navbar-collapse collapse">
                        <ul class="nav navbar-nav">
                            <li><a href="#">Home</a></li>
                            <li class="active"><a href="simulation.jsp">Simulation</a></li>
                            <li><a href="about.jsp">About</a></li>
                        </ul>
                    </div>
                </div>
            </nav>

        </div>
    </div>

    <div class="container">
        <div class="col-lg-4">
            <form class="form-signin" action="startSimulation">
                <h2 class="form-signin-heading">Simulation features</h2>

                <div class="col-lg-8">
                    <label for="vmNumber" class="sr-only">VM Number</label>
                    <input type="number" id="vmNumber" class="form-control" placeholder="VM Number" required
                           autofocus><br>
                    <label for="hostNumber" class="sr-only">Host Number</label>
                    <input type="number" id="hostNumber" class="form-control" placeholder="Host Number" required><br>
                    <label for="simulationPeriod" class="sr-only"></label>
                    <select class="form-control" id="simulationPeriod">
                        <option disabled selected>Simulation Period</option>
                        <option value="6">6 hours</option>
                        <option value="12">12 hours</option>
                        <option value="18">18 hours</option>
                        <option value="24">24 hours</option>
                    </select><br>
                    <select class="form-control" id="simulationType">
                        <option disabled selected>Simulation Type</option>
                        <option value="Green Energy">Green Energy</option>
                        <option value="Brown Energy">Brown Energy</option>
                        <option value="Latency">Latency</option>
                    </select><br>
                    <button class="btn btn-lg btn-primary btn-block" type="submit">Start Simulation!</button>
                </div>
            </form>
        </div>
        <div class="col-lg-8"></div>
    </div>
    <!-- /container -->
</div>


<!-- Bootstrap core JavaScript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
<script src="../../resources/dist/js/bootstrap.min.js"></script>
<!-- Just to make our placeholder images work. Don't actually copy the next line! -->
<script src="../../resources/assets/js/vendor/holder.js"></script>
</body>
</html>
