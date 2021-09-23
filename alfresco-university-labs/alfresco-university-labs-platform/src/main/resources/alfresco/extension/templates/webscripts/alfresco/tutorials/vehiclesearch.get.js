var docquery = "select * from col:vehicle";
var def = {
    query: docquery,
    language: "cmis-alfresco"
};
var results = search.query(def);
if(results!=null && results.length>0) {
    model.equipmentList = results;
} else {
    status.redirect=true;
    status.code=500;
    status.message="No docs of type Colt Vehicle were found.";
}