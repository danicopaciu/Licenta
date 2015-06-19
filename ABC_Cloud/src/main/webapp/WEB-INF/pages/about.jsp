<%@ include file="/WEB-INF/pages/includes_CSS.jsp" %>
<html>
<head>
    <title>Energy efficiency in data centers</title>
</head>
<body>
  <div class="navbar-wrapper">
    <div class="container">
      <nav class="navbar navbar-inverse navbar-static-top">
        <div class="container">
          <div id="navbar" class="navbar-collapse collapse">
            <ul class="nav navbar-nav">
              <li><a hraf="#">Energy efficiency in data centers</a></li>
              <li><c:url value="/" var="home"/><a href="${home}">Home</a></li>
              <li><c:url value="/simulation" var="simulation"/><a href="${simulation}">Simulation</a></li>
              <li class="active"><c:url value="/about" var="about"/><a href="${about}">About</a></li>
            </ul>
          </div>
        </div>
      </nav>
    </div>
  </div>
  <div class="container about_container">
    <h1>Simulator utilization</h1>

    <div class="col-lg-12">
      <h2>Hardware resources</h2>
      <ul>
        <li>1 GB RAM memory</li>
        <li>Dual-Core processor</li>
      </ul>
    </div>

    <div class="col-lg-12">
      <h2>Software resources</h2>
      <ul>
        <li>OS Windows 7/8/8.1</li>
        <li>Tomcat 7 or a newer version</li>
        <li>Java JDK 1.8 or a newer version</li>
        <li>Web Browser Chrome or Firefox</li>
      </ul>
    </div>

    <div class="col-lg-12">
      <h2>Installation and getting started</h2>
      <ol>
        <li>Stop the Tomcat server.</li>
        <li>Copy the supplied WAR file into the TOMCAT_HOME/webapps directory</li>
        <li>Start the Tomcat server</li>
        <li>Navigate in the web browser to the address http://localhost:8080/</li>
      </ol>
    </div>

    <div class="col-lg-12">
      <h2>Utilization</h2>
      <p class="about_p">
        After navigating to the specified address user will be greeted by a welcome page shown in Figure 1.
        In this page the user can choose which page to nagivate to: to the About page or to the Simulation page. To browse to the About page
        the user will click the About button . To browse to the page simulation, he needs to click on Simulation button.
      </p>
      <figure>
        <img class="about_img" src="/resources/interface1.png">
        <figcaption class="about_caption">Figure 1</figcaption>
      </figure>
      <p class="about_p">
        The simulation page runs the algorithm using data provided by the user. In Figure 2 is presented the Simulation page.
        To start the simulation the user must enter the required data: the number of virtual machines in VM number field, the number of servers
        per data center in Host number field, the simulation period in Simulation period field and
        then choose the algorithm parameter variation from the provided list. After this step the user must press the Start Simulation button,
        shown in Figure 3.
      </p>
      <figure>
        <img class="about_img" src="/resources/interface2.png">
        <figcaption class="about_caption">Figure 2</figcaption>
      </figure>
      <figure>
        <img class="about_img" src="/resources/interface3.png">
        <figcaption class="about_caption">Figure 3</figcaption>
      </figure>
      <p class="about_p">
        After the simulation is over, under the radial progress bars the results will be shown. They are consisted of a table
        with general all the statistics obtained during the simulation and a graphic which shows the variation of energies or
        number of VMs. The results are shown in Figures 4 and 5.
      </p>
      <figure>
        <img class="about_img" src="/resources/interface4.png">
        <figcaption class="about_caption">Figure 4</figcaption>
      </figure>
      <figure>
        <img class="about_img" src="/resources/interface5.png">
        <figcaption class="about_caption">Figure 5</figcaption>
      </figure>
    </div>
  </div>
</body>
<%@ include file="/WEB-INF/pages/includes_JS.jsp" %>

</html>
