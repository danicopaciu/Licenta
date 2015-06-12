<%@ include file="/WEB-INF/pages/includes_CSS.jsp" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">
    <title>Eficienta Energetica In Retelele De Centre De Date</title>

</head>
<body>
<div class="navbar-wrapper">
    <div class="container">
        <nav class="navbar navbar-inverse navbar-static-top">
            <div class="container">
                <div class="navbar-header">
                    <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar"
                            aria-expanded="false" aria-controls="navbar">
                        <span class="sr-only">Toggle navigation</span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>
                    <a class="navbar-brand" href="#">Project name</a>
                </div>
                <div id="navbar" class="navbar-collapse collapse">
                    <ul class="nav navbar-nav">
                        <li class="active"><a href="#">Home</a></li>
                        <li><c:url value="/simulation" var="simulation"/><a href="${simulation}">Simulation</a></li>
                        <li><c:url value="/about" var="about"/><a href="${about}">About</a></li>
                    </ul>
                </div>
            </div>
        </nav>

    </div>
</div>


<!-- Carousel
================================================== -->
<div id="myCarousel" class="carousel slide" data-ride="carousel">
    <!-- Indicators -->
    <ol class="carousel-indicators">
        <li data-target="#myCarousel" data-slide-to="0" class="active"></li>
        <li data-target="#myCarousel" data-slide-to="1"></li>
        <li data-target="#myCarousel" data-slide-to="2"></li>
    </ol>
    <div class="carousel-inner" role="listbox">
        <div class="item active">
            <img class="first-slide" src="resources/datacenterfacebook.jpg" alt="First slide">

            <div class="container">
                <div class="carousel-caption">
                    <h1>Facebook:</h1>
                    <p>The best way to reduce CO2 and improve the environment is to cut energy consumption and that is
                        what we are doing</p>
                </div>
            </div>
        </div>
        <div class="item">
            <img class="second-slide" src="resources/datacentergoogle.jpg" alt="Second slide">

            <div class="container">
                <div class="carousel-caption">
                    <h1>Google:</h1>
                    <p>A sustainable data center starts with making our computers use as little electricity as
                        possible.</p>
                </div>
            </div>
        </div>
        <div class="item">
            <img class="third-slide" src="resources/googlerenewableenergy.jpg" alt="Third slide">

            <div class="container">
                <div class="carousel-caption">
                    <h1>Greenpeace:</h1>
                    <p>Given the massive amounts of electricity that even energy-efficient data centers consume to run
                        computers, backup
                        power units, and power related cooling equipment, the last thing we need to be doing is building
                        them in places
                        where they are increasing demand for dirty coal-fired power</p>
                </div>
            </div>
        </div>
    </div>
    <a class="left carousel-control" href="#myCarousel" role="button" data-slide="prev">
        <span class="glyphicon glyphicon-chevron-left" aria-hidden="true"></span>
        <span class="sr-only">Previous</span>
    </a>
    <a class="right carousel-control" href="#myCarousel" role="button" data-slide="next">
        <span class="glyphicon glyphicon-chevron-right" aria-hidden="true"></span>
        <span class="sr-only">Next</span>
    </a>
</div>
<!-- /.carousel -->

<div class="container marketing">

    <div class="row">
        <div class="col-lg-6">
            <div class="index_link">
                <c:url value="/simulation" var="simulation"/>
                <a class="" href="${simulation}">
                    <img class="img-circle" src="resources/simulation.png" alt="Generic placeholder image" width="140" height="140">
                </a>
                <h2>Simulation</h2>
            </div>
        </div>
        <div class="col-lg-6">
            <div class="index_link">
                <c:url value="/about" var="about"/>
                <a class="" href="${about}">
                <img class="img-circle" src="resources/about.png" alt="Generic placeholder image" width="140"
                     height="140">
                </a>
                <h2>About</h2>
            </div>
        </div>
    </div>

    <!-- FOOTER -->
    <footer>
        <%--<p class="pull-right"><a href="#">Back to top</a></p>--%>

        <%--<p>&copy; 2014 Company, Inc. &middot; <a href="#">Privacy</a> &middot; <a href="#">Terms</a></p>--%>
    </footer>
    </div>

</body>

<%@ include file="/WEB-INF/pages/includes_JS.jsp" %>

</html>
