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


//vis.selectAll(".line")
//    .data(links)
//    .enter()
//    .append("path")
//    .attr("id", function(d) { return "line_"+d.source.id+d.target.id; })
//    .attr("x1", function(d) { return d.source.x })
//    .attr("y1", function(d) { return d.source.y })
//    .attr("x2", function(d) { return d.target.x })
//    .attr("y2", function(d) { return d.target.y })
//    .style("stroke", "rgb(6,120,155)")
//    .style("stroke-width", 4)


var path = vis.selectAll("path")
    .data(links)
    .enter().append("svg:path")
    .attr("class", function(d) { return "link"})
    .attr("id",function(d,i) { return "linkid_" + i; })
    //.attr("marker-end", function(d) { return "url(#" + d.source.x + ")"; });

path.attr("d", function(d) {
    var dx = d.target.x - d.source.x,
        dy = d.target.y - d.source.y,
        dr = Math.sqrt(dx * dx + dy * dy);  //linknum is defined above
    return "M" + d.source.x + "," + d.source.y + "A" + dr + "," + dr + " 0 0,1 " + d.target.x + "," + d.target.y;
    //return "M" + 20 + "," + 20 + "A" + dr + "," + dr + " 0 0,1 " + ((d.source.x+ d.target.x)/2) + "," + ((d.source.y+ d.target.y)/2);
    //return "M" + d.source.x + " " + d.source.y + "Q" + ((d.source.x+ d.target.x)/2 +40) + " " + ((d.source.y+ d.target.y)/2 +40)+ " "  + d.target.x + " " + d.target.y;
});


vis.selectAll("circle.nodes")
    .data(nodes)
    .enter()
    .append("svg:circle")
    .attr("cx", function(d) { return d.x; })
    .attr("cy", function(d) { return d.y; })
    .attr("r", 75)
    .attr("id", function(d) { return "dc_"+d.id; })
//    .attr("fill", "black")
    .attr("fill", "url(#image)")
    //.style("stroke", "black")     // displays small black dot


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


    $( "#dc_0" ).click(function() {
        $.ajax({
            url : 'getStatistics/0',
            success : function(data) {

                console.log(data);

                var results = jQuery.parseJSON(data);
                var dc_0 = results[0];

                $(".simulation_time span").text(dc_0.clock);

                console.log(dc_0);

                 rp1.minValue(0)
                    .maxValue(dc_0.greenEnergy)
                    .value(dc_0.serverEnergy)
                    .render();
                rp2.minValue(0)
                    .maxValue(dc_0.overallVms)
                    .value(dc_0.dcVms)
                    .render();
                rp3.minValue(0)
                    .maxValue(dc_0.VmsIn)
                    .value(dc_0.VmsIn)
                    .render();
            }
        });
    });

});