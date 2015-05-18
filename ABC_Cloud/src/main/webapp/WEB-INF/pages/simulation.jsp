<%@ include file="/WEB-INF/pages/includes_CSS.jsp" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <meta name="description" content="">
    <meta name="author" content="">

    <title>Carousel Template for Bootstrap</title>

</head>
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

    <div id="main_container" class="container">
        <div id="form_container" class="col-lg-3">
            <h2 class="form-header">Simulation features</h2>

            <form class="form-signin" action="startSimulation">
                <div class="form-group">
                    <label for="vmNumber">VM Number</label>
                    <input type="number" min='1' id="vmNumber" class="form-control" placeholder="VM Number" required name="vmNumber" value="800">
                </div>
                <div class="form-group">
                    <label for="hostNumber">Host Number</label>
                    <input type="number" min='1' id="hostNumber" class="form-control" placeholder="Host Number" required name="hostNumber" value="200">
                </div>
                <div class="form-group">
                    <label for="simulationPeriod">Simulation period</label>
                    <select class="form-control" id="simulationPeriod" name="simulationPeriod">
                        <option value="6" selected="selected">6 hours</option>
                        <option value="12">12 hours</option>
                        <option value="18">18 hours</option>
                        <option value="24">24 hours</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="simulationType">Simulation type</label>
                    <select class="form-control" id="simulationType" name="simulationType">
                        <option value="Green Energy" selected="selected">Green Energy</option>
                        <option value="Brown Energy">Brown Energy</option>
                        <option value="Latency">Latency</option>
                    </select>
                </div>
                <button id="startButton" formmethod="post" class="btn btn-lg btn-primary btn-block" type="submit">Start Simulation!</button>
            </form>
        </div>
        <div id="simulation_container" class="col-lg-9">
            <div class="col-lg-12 simulation_time">
                <label>Time: </label><span>0</span>
                <div class="progress">
                    <div class="progress-bar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%;">
                        60%
                    </div>
                </div>
            </div>

            <div id="graph_container" class="col-lg-12">
                <svg id="imgSVG" width="0" height="0">
                    <defs id="mdef">
                        <pattern id="image" x="0" y="0" height="1" width="1">
                            <image x="-10" y="-10" width="160" height="150" xlink:href="resources/datacenter1.png"></image>
                        </pattern>
                    </defs>
                </svg>
                    <%--<filter id = "i1" x = "0%" y = "0%" width = "100%" height = "100%">--%>
                        <%--<feImage xlink:href = "server.png"/>--%>
                    <%--</filter>--%>

            </div>
            <div id="gouge_container" class="col-lg-12">
                <div id="pw1" class="col-lg-4">

                </div>
                <div id="pw2" class="col-lg-4">

                </div>
                <div id="pw3" class="col-lg-4">


                </div>
            </div>
        </div>
    </div>
</div>
</body>
<%@ include file="/WEB-INF/pages/includes_JS.jsp" %>
</html>
