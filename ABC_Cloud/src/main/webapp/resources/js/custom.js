var vis = d3.select("#graph_container")
    .append("svg");

vis.attr("id", "dc_graph");
vis.attr("width", "100%").attr("height", 500);

vis.text("Our Graph")
    .select("#graph")

var nodes = [
    {x: 420, y: 250, id: 0},
    {x: 100, y: 100, id: 1},
    {x: 740, y: 100, id: 2},
    {x: 740, y: 400, id: 3},
    {x: 100, y: 400, id: 4}]

var links = [
    {source: nodes[0], target: nodes[1]},
    {source: nodes[0], target: nodes[2]},
    {source: nodes[0], target: nodes[3]},
    {source: nodes[0], target: nodes[4]},
    {source: nodes[1], target: nodes[2]},
    {source: nodes[2], target: nodes[3]},
    {source: nodes[3], target: nodes[4]},
    {source: nodes[4], target: nodes[1]}]

var path = vis.selectAll("path")
    .data(links)
    .enter().append("svg:path")
    .attr("class", function(d) { return "link"})
    .attr("id",function(d,i) { return "linkid_" + i; })

path.attr("d", function(d) {
    var dx = d.target.x - d.source.x,
        dy = d.target.y - d.source.y,
        dr = Math.sqrt(dx * dx + dy * dy);  //linknum is defined above
    return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
});


vis.selectAll("circle.nodes")
    .data(nodes)
    .enter()
    .append("svg:circle")
    .attr("cx", function(d) { return d.x; })
    .attr("cy", function(d) { return d.y; })
    .attr("r", 75)
    .attr("id", function(d) { return "dc_"+d.id; })
    .attr("fill", "url(#image)")

$( document ).ready(function() {

    var rp1 = radialProgress(document.getElementById('pw1'))
            .label("Server Eng/ Green Eng")
            .diameter(150)
            .minValue(0)
            .maxValue(100)
            .value(0)
            .render();
    var rp2 = radialProgress(document.getElementById('pw2'))
            .label("Dc VMs / Overall VMs")
            .diameter(150)
            .minValue(0)
            .maxValue(100)
            .value(0)
            .render();
    var rp3 = radialProgress(document.getElementById('pw3'))
            .label("VMs IN / VMs migrated")
            .diameter(150)
            .minValue(0)
            .maxValue(100)
            .value(0)
            .render();

    $( "#startButton" ).click(function() {
        setTimeout(getTime, 5000);
    });

    $( "#dc_0" ).click(function() {
        $.ajax({
            url : 'getStatistics/0',
            success : function(data) {

                console.log(data);

                var results = jQuery.parseJSON(data);

                $(".simulation_time span").text(results.clock);
                $('.vm_status').show();
                $('.vm_status .vms_in span').text(results.VmsIn);
                $('.vm_status .vms_out span').text(results.VmsOut);
                $('path.arc2').hide();


                var migrated_vms = results.migratedVms;
                var vms_in = results.VmsIn;
                if (migrated_vms == 0){
                    vms_in = 0;
                    migrated_vms = 1;
                }

                rp1.minValue(0)
                    .maxValue(results.greenEnergy)
                    .value(results.serverEnergy)
                    .render();
                rp2.minValue(0)
                    .maxValue(results.overallVms)
                    .value(results.dcVms)
                    .render();
                rp3.minValue(0)
                    .maxValue(migrated_vms)
                    .value(vms_in)
                    .render();
            }
        });
    });

});

 // 1000 = 1 second, 3000 = 3 seconds
function getTime() {
    $.ajax({
        url: 'simulationProgress',
        success: function (data) {
            $('.progress-bar').attr("aria-valuenow",data);
            $('.progress-bar').text(data+" %");
            $('.progress-bar').width(data+"%");
        },
        complete: function (data) {
            // Schedule the next
            setTimeout(getTime, 500);
        }
    });
}


function InitChart(data) {
    //var data = [{"name":"GreenEnergy","time":600.5,"energy":3037500.1},{"name":"ServerEnergy","time":600.5,"energy":220148.83},{"name":"GreenEnergy","time":900.5,"energy":2970000.1},{"name":"ServerEnergy","time":900.5,"energy":222666.66},{"name":"GreenEnergy","time":1200.5,"energy":2902500.1},{"name":"ServerEnergy","time":1200.5,"energy":558323.52},{"name":"GreenEnergy","time":1500.5,"energy":2835000.1},{"name":"ServerEnergy","time":1500.5,"energy":559849.48},{"name":"GreenEnergy","time":1800.5,"energy":2767500.1},{"name":"ServerEnergy","time":1800.5,"energy":560410.48},{"name":"GreenEnergy","time":2100.5,"energy":2700000.1},{"name":"ServerEnergy","time":2100.5,"energy":559968.69},{"name":"GreenEnergy","time":2400.5,"energy":2632500.1},{"name":"ServerEnergy","time":2400.5,"energy":560970.79},{"name":"GreenEnergy","time":2700.5,"energy":2565000.1},{"name":"ServerEnergy","time":2700.5,"energy":560373.4},{"name":"GreenEnergy","time":3000.5,"energy":2497500.1},{"name":"ServerEnergy","time":3000.5,"energy":557401.65},{"name":"GreenEnergy","time":3300.5,"energy":2430000.1},{"name":"ServerEnergy","time":3300.5,"energy":557318.25},{"name":"GreenEnergy","time":3600.5,"energy":2362500.1},{"name":"ServerEnergy","time":3600.5,"energy":558414.48},{"name":"GreenEnergy","time":3900.5,"energy":2092500.1},{"name":"ServerEnergy","time":3900.5,"energy":558271.87},{"name":"GreenEnergy","time":4200.5,"energy":1890000.1},{"name":"ServerEnergy","time":4200.5,"energy":559410.85},{"name":"GreenEnergy","time":4500.5,"energy":1620000.1},{"name":"ServerEnergy","time":4500.5,"energy":560203.25},{"name":"GreenEnergy","time":4800.5,"energy":1417500.1},{"name":"ServerEnergy","time":4800.5,"energy":558986.22},{"name":"GreenEnergy","time":5100.5,"energy":1147500.1},{"name":"ServerEnergy","time":5100.5,"energy":558503.89},{"name":"GreenEnergy","time":5400.5,"energy":945000.1},{"name":"ServerEnergy","time":5400.5,"energy":558512.68},{"name":"GreenEnergy","time":5700.5,"energy":675000.1},{"name":"ServerEnergy","time":5700.5,"energy":559450.71},{"name":"GreenEnergy","time":6000.5,"energy":1350000.1},{"name":"ServerEnergy","time":6000.5,"energy":529813.65},{"name":"GreenEnergy","time":6300.5,"energy":675000.1},{"name":"ServerEnergy","time":6300.5,"energy":529557.79},{"name":"GreenEnergy","time":6600.5,"energy":5400000.1},{"name":"ServerEnergy","time":6600.5,"energy":499459.74},{"name":"GreenEnergy","time":6900.5,"energy":6480000.1},{"name":"ServerEnergy","time":6900.5,"energy":498639.72},{"name":"GreenEnergy","time":7200.5,"energy":6277500.1},{"name":"ServerEnergy","time":7200.5,"energy":557736.63},{"name":"GreenEnergy","time":7500.5,"energy":6345000.1},{"name":"ServerEnergy","time":7500.5,"energy":559436.33},{"name":"GreenEnergy","time":7800.5,"energy":6412500.1},{"name":"ServerEnergy","time":7800.5,"energy":558809.13},{"name":"GreenEnergy","time":8100.5,"energy":6547500.1},{"name":"ServerEnergy","time":8100.5,"energy":558215.92},{"name":"GreenEnergy","time":8400.5,"energy":6615000.1},{"name":"ServerEnergy","time":8400.5,"energy":557744.85},{"name":"GreenEnergy","time":8700.5,"energy":4320000.1},{"name":"ServerEnergy","time":8700.5,"energy":557369.09},{"name":"GreenEnergy","time":9000.5,"energy":135000.1},{"name":"ServerEnergy","time":9000.5,"energy":557864.34},{"name":"GreenEnergy","time":9300.5,"energy":202500.1},{"name":"ServerEnergy","time":9300.5,"energy":218475.78},{"name":"GreenEnergy","time":9600.5,"energy":405000.1},{"name":"ServerEnergy","time":9600.5,"energy":137837.96},{"name":"GreenEnergy","time":9900.5,"energy":540000.1},{"name":"ServerEnergy","time":9900.5,"energy":137668.28},{"name":"GreenEnergy","time":10200.5,"energy":1012500.1},{"name":"ServerEnergy","time":10200.5,"energy":222274.16},{"name":"GreenEnergy","time":10500.5,"energy":810000.1},{"name":"ServerEnergy","time":10500.5,"energy":308175.07},{"name":"GreenEnergy","time":10800.5,"energy":1012500.1},{"name":"ServerEnergy","time":10800.5,"energy":444514.16},{"name":"GreenEnergy","time":11100.5,"energy":1687500.1},{"name":"ServerEnergy","time":11100.5,"energy":500192.08},{"name":"GreenEnergy","time":11400.5,"energy":2362500.1},{"name":"ServerEnergy","time":11400.5,"energy":501247.81},{"name":"GreenEnergy","time":11700.5,"energy":6412500.1},{"name":"ServerEnergy","time":11700.5,"energy":528545.26},{"name":"GreenEnergy","time":12000.5,"energy":5940000.1},{"name":"ServerEnergy","time":12000.5,"energy":528674.82},{"name":"GreenEnergy","time":12300.5,"energy":5535000.1},{"name":"ServerEnergy","time":12300.5,"energy":528689.84},{"name":"GreenEnergy","time":12600.5,"energy":5062500.1},{"name":"ServerEnergy","time":12600.5,"energy":528361.41},{"name":"GreenEnergy","time":12900.5,"energy":4590000.1},{"name":"ServerEnergy","time":12900.5,"energy":529720.88},{"name":"GreenEnergy","time":13200.5,"energy":4185000.1},{"name":"ServerEnergy","time":13200.5,"energy":529364.96},{"name":"GreenEnergy","time":13500.5,"energy":3712500.1},{"name":"ServerEnergy","time":13500.5,"energy":527808.32},{"name":"GreenEnergy","time":13800.5,"energy":3307500.1},{"name":"ServerEnergy","time":13800.5,"energy":527112.89},{"name":"GreenEnergy","time":14100.5,"energy":2835000.1},{"name":"ServerEnergy","time":14100.5,"energy":527836.75},{"name":"GreenEnergy","time":14400.5,"energy":2430000.1},{"name":"ServerEnergy","time":14400.5,"energy":528666.92},{"name":"GreenEnergy","time":14700.5,"energy":2430000.1},{"name":"ServerEnergy","time":14700.5,"energy":529494.95},{"name":"GreenEnergy","time":15000.5,"energy":2362500.1},{"name":"ServerEnergy","time":15000.5,"energy":529486.79},{"name":"GreenEnergy","time":15300.5,"energy":2362500.1},{"name":"ServerEnergy","time":15300.5,"energy":529855.0},{"name":"GreenEnergy","time":15600.5,"energy":2362500.1},{"name":"ServerEnergy","time":15600.5,"energy":528967.17},{"name":"GreenEnergy","time":15900.5,"energy":2362500.1},{"name":"ServerEnergy","time":15900.5,"energy":527387.66},{"name":"GreenEnergy","time":16200.5,"energy":2362500.1},{"name":"ServerEnergy","time":16200.5,"energy":528588.38},{"name":"GreenEnergy","time":16500.5,"energy":2430000.1},{"name":"ServerEnergy","time":16500.5,"energy":531220.89},{"name":"GreenEnergy","time":16800.5,"energy":2430000.1},{"name":"ServerEnergy","time":16800.5,"energy":531126.59},{"name":"GreenEnergy","time":17100.5,"energy":2497500.1},{"name":"ServerEnergy","time":17100.5,"energy":529430.38},{"name":"GreenEnergy","time":17400.5,"energy":2497500.1},{"name":"ServerEnergy","time":17400.5,"energy":528964.19},{"name":"GreenEnergy","time":17700.5,"energy":2565000.1},{"name":"ServerEnergy","time":17700.5,"energy":529669.76},{"name":"GreenEnergy","time":18000.5,"energy":2632500.1},{"name":"ServerEnergy","time":18000.5,"energy":530623.27},{"name":"GreenEnergy","time":18300.5,"energy":2700000.1},{"name":"ServerEnergy","time":18300.5,"energy":530225.48},{"name":"GreenEnergy","time":18600.5,"energy":2835000.1},{"name":"ServerEnergy","time":18600.5,"energy":531872.31},{"name":"GreenEnergy","time":18900.5,"energy":2902500.1},{"name":"ServerEnergy","time":18900.5,"energy":531579.3},{"name":"GreenEnergy","time":19200.5,"energy":3037500.1},{"name":"ServerEnergy","time":19200.5,"energy":504978.37},{"name":"GreenEnergy","time":19500.5,"energy":3172500.1},{"name":"ServerEnergy","time":19500.5,"energy":505175.6},{"name":"GreenEnergy","time":19800.5,"energy":3240000.1},{"name":"ServerEnergy","time":19800.5,"energy":501526.04},{"name":"GreenEnergy","time":20100.5,"energy":3375000.1},{"name":"ServerEnergy","time":20100.5,"energy":500832.02},{"name":"GreenEnergy","time":20400.5,"energy":3442500.1},{"name":"ServerEnergy","time":20400.5,"energy":501265.69},{"name":"GreenEnergy","time":20700.5,"energy":3577500.1},{"name":"ServerEnergy","time":20700.5,"energy":501407.42},{"name":"GreenEnergy","time":21000.5,"energy":3712500.1},{"name":"ServerEnergy","time":21000.5,"energy":500581.36},{"name":"GreenEnergy","time":21300.5,"energy":3780000.1},{"name":"ServerEnergy","time":21300.5,"energy":500756.3},{"name":"GreenEnergy","time":21600.5,"energy":3915000.1},{"name":"ServerEnergy","time":21600.5,"energy":501808.19}] ;
//
    var vis = d3.select("#visualisation"),
        WIDTH = 1000,
        HEIGHT = 500,
        MARGINS = {
            top: 50,
            right: 20,
            bottom: 50,
            left: 75
        },

      xScale = d3.scale.linear().range([MARGINS.left, WIDTH - MARGINS.right]).domain([d3.min(data, function (d) {
            return d.time;
        }), d3.max(data, function(d) {
            return d.time;
        })]),
        yScale = d3.scale.linear().range([HEIGHT - MARGINS.top, MARGINS.bottom]).domain([d3.min(data, function(d) {
          return d.val;
        }), d3.max(data, function(d) {
          return d.val;
        })]),
        xAxis = d3.svg.axis()
            .scale(xScale),
        yAxis = d3.svg.axis()
            .scale(yScale)
            .orient("left");

    var dataGroup = d3.nest()
        .key(function(d) {
            return d.name;
        })
        .entries(data);


    lSpace = WIDTH/dataGroup.length;

    vis.append("svg:g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + (HEIGHT - MARGINS.bottom) + ")")
        .call(xAxis);
    vis.append("svg:g")
        .attr("class", "y axis")
        .attr("transform", "translate(" + (MARGINS.left) + ",0)")
        .call(yAxis);
    var lineGen = d3.svg.line()
        .x(function(d) {
            return xScale(d.time);
        })
        .y(function(d) {
        return yScale(d.val);
        })
        .interpolate("basis");


    var colors = ["blue", "red", "limegreen", "cyan", "#8B4500"];

    dataGroup.forEach(function(d, i) {

        vis.append("text")
            .attr("x", (lSpace / 2) + i * lSpace)
            .attr("y", HEIGHT-5)
            .style("fill", "black")
            .text(d.key)
            .attr("class", d.key);

//        var legend_elem_color = "hsl(" + Math.random() * 360 + ",100%,50%)"
        var legend_elem_color = colors[i];
        var legend_elem_width = $("." + d.key).width();

        vis.append("line")
            .attr("x1", (lSpace / 2) + i * lSpace + legend_elem_width + 10)
            .attr("y1", HEIGHT - 10)
            .attr("x2", (lSpace / 2) + i * lSpace + legend_elem_width + 45)
            .attr("y2", HEIGHT - 10)
            .style("stroke", legend_elem_color)
            .attr("stroke-width", 5);

        vis.append('svg:path')
            .attr('d', lineGen(d.values))
            .attr('stroke',legend_elem_color)
            .attr('stroke-width', 2)
            .attr('fill', 'none');
    });

}
