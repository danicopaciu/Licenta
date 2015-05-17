var vis = d3.select("#graph_container")
    .append("svg");

vis.attr("id", "dc_graph");
vis.attr("width", "100%").attr("height", 500);

vis.text("Our Graph")
    .select("#graph")

var nodes = [
    {x: 420, y: 250, id: "dc_0"},
    {x: 100, y: 100, id: "dc_1"},
    {x: 740, y: 100, id: "dc_2"},
    {x: 740, y: 400, id: "dc_3"},
    {x: 100, y: 400, id: "dc_4"}]

var links = [
    {source: nodes[0], target: nodes[1]},
    {source: nodes[0], target: nodes[2]},
    {source: nodes[0], target: nodes[3]},
    {source: nodes[0], target: nodes[4]},
    {source: nodes[1], target: nodes[2]},
    {source: nodes[2], target: nodes[3]},
    {source: nodes[3], target: nodes[4]},
    {source: nodes[4], target: nodes[1]}]


vis.selectAll(".line")
    .data(links)
    .enter()
    .append("line")
    .attr("x1", function(d) { return d.source.x })
    .attr("y1", function(d) { return d.source.y })
    .attr("x2", function(d) { return d.target.x })
    .attr("y2", function(d) { return d.target.y })
    .style("stroke", "rgb(6,120,155)")
    .style("stroke-width", 4)

vis.selectAll("circle.nodes")
    .data(nodes)
    .enter()
    .append("svg:circle")
    .attr("cx", function(d) { return d.x; })
    .attr("cy", function(d) { return d.y; })
    .attr("r", 40)
    .attr("id", function(d) { return d.id; })
//    .attr("fill", "black")
    .attr("fill", "url(#image)")
//    .attr("filter","url(#i1)")
    .style("stroke", "black")     // displays small black dot
    .style("stroke-width", 0.25)

//
//vis.append("circle")
//    .attr("class", "logo")
//    .attr("cx", 225)
//    .attr("cy", 225)
//    .attr("r", 20)
//    .style("fill", "url(#image)")       // this code works OK
//    .style("stroke", "black")     // displays small black dot
//    .style("stroke-width", 0.25)
//         .on("mouseover", function(){ // when I use .style("fill", "red") here, it works 
//               d3.select(this)
//                   .style("fill", "url(#image)");
//         })
//          .on("mouseout", function(){ 
//               d3.select(this)
//                   .style("fill", "transparent");
//         });


//var rp3 = radialProgress(document.getElementById('div3'))



//
//var elems =vis.selectAll("circle");
//res = elems.every(function(element, index, elems) {
//  console.log('element:', element);
//  element.attr("class","test"+index);
////  if (element >= THRESHOLD) {
////    return false;
////  }
//      return true;
//});
$( document ).ready(function() {
    var rp1 = radialProgress(document.getElementById('pw1'))
            .label("Server eng/ Green energy")
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
            url : 'ajaxCall',
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