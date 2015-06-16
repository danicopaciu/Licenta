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

    <title>Simulare</title>

</head>
<body>
<div class="row-margin">
    <div class="navbar-wrapper">
        <div class="container">
            <nav class="navbar navbar-inverse navbar-static-top">
                <div class="container">
                    <div id="navbar" class="navbar-collapse collapse">
                        <ul class="nav navbar-nav">
                            <li><c:url value="/" var="home"/><a href="${home}">Home</a></li>
                            <li class="active"><c:url value="/simulation" var="simulation"/><a href="${simulation}">Simulation</a></li>
                            <li><c:url value="/about" var="about"/><a href="${about}">About</a></li>
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
                    <input type="number" min='1' id="vmNumber" class="form-control" placeholder="VM Number" required name="vmNumber" value="400">
                </div>
                <div class="form-group">
                    <label for="hostNumber">Host Number</label>
                    <input type="number" min='1' id="hostNumber" class="form-control" placeholder="Host Number" required name="hostNumber" value="100">
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
                        <option value="1" selected="selected">Consumed Energy Variation</option>
                        <option value="2">Heat Variation</option>
                        <option value="3">Latency Variation</option>
                        <option value="4">Energy Cost Variation</option>
                    </select>
                </div>
                <button id="startButton" formmethod="post" class="btn btn-lg btn-primary btn-block" type="submit">Start Simulation!</button>
            </form>
        </div>
        <div id="simulation_container" class="col-lg-9">
            <div class="col-lg-12 simulation_time">
                <label>Last Time: </label><span>0</span>
                <div class="progress">
                    <div class="progress-bar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%;">
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

                <div class="vm_status">
                    <div class="vm_status_area">
                        <span class="glyphicon glyphicon-arrow-up " aria-hidden="true"></span>
                    </div>
                    <div class="vm_status_area vms_in">
                        <span class="">0</span>
                    </div>
                    <div class="vm_status_area">
                        <span class="glyphicon glyphicon-arrow-down" aria-hidden="true"></span>
                    </div>
                    <div class="vm_status_area vms_out">
                        <span class="">0</span>
                    </div>
                </div>

                <div class="wind_icon">
                    <img src="/resources/wind_icon.png">
                </div>

                <div class="brown_icon brown_icon1">
                    <img src="/resources/brown_icon.png">
                </div>

                <div class="brown_icon brown_icon2">
                    <img src="/resources/brown_icon.png">
                </div>

                <div class="brown_icon brown_icon3">
                    <img src="/resources/brown_icon.png">
                </div>

                <div class="brown_icon brown_icon4">
                    <img src="/resources/brown_icon.png">
                </div>


            </div>
            <div id="gouge_container" class="col-lg-12">
                <div id="pw1" class="col-lg-4"></div>
                <div id="pw2" class="col-lg-4"> </div>
                <div id="pw3" class="col-lg-4"></div>
            </div>
        </div>
        <c:if test="${!empty result}">
            <div class="grafic_container col-lg-12">
                <h2>Graphic</h2>
                <svg id="visualisation" width="100%" height="500"></svg>
            </div>
            <div class="col-lg-12">
                <h2>Statistics</h2>
                <table class="table">
                    <tr>
                        <th>Time</th>
                        <th>Green Energy</th>
                        <th>Brown Energy</th>
                        <th>Energy consumed by servers</th>
                        <th>Energy consumed by CRAC</th>
                        <th>Heat Gained</th>
                        <th>VMs Migrated In</th>
                        <th>VMs Migrated Out</th>
                        <th>Total VMs in Data Center</th>
                    </tr>
                    <c:forEach var="timeList" items="${result}">
                        <tr>
                            <td>${timeList.key}</td>
                            <c:forEach var="data" items="${timeList.value}">
                                <td>${data.value}</td>
                            </c:forEach>
                        </tr>
                        <c:forEach var="data" items="${timeList.value}">
                        </c:forEach>
                    </c:forEach>
                </table>
            </div>
        </c:if>
    </div>
</div>
</body>
<%@ include file="/WEB-INF/pages/includes_JS.jsp" %>


<script>
    var table_results = ${json_result} ;
    $( document ).ready(function() {
        InitChart(table_results);
    });
</script>

</html>
