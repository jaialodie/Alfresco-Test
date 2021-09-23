[#ftl]
vehicles:
[
   [#list equipmentList as e]
        [#escape x as jsonUtils.encodeJSONString(x)]
   {
           "name": "${e.name}"
   } [#if e_has_next],[/#if]
        [/#escape]
   [/#list]
]