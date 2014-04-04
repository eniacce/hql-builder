package org.tools.hqlbuilder.webcommon.resteasy;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import org.tools.hqlbuilder.common.ExecutionResult;
import org.tools.hqlbuilder.common.QueryParameter;
import org.tools.hqlbuilder.common.QueryParameters;
import org.tools.hqlbuilder.common.XmlWrapper;

@Path("/pojo")
@Pretty
@GZIP
public interface PojoResource {
    public static final String TEXT = MediaType.TEXT_PLAIN;
    public static final String BINARY = MediaType.APPLICATION_OCTET_STREAM;
    public static final String XML = MediaType.TEXT_XML;

    /**
     * @see [get] http://localhost:80/hqlbuilder/rest/pojo/ping
     */
    @GET
    @Path("/ping")
    @Produces({ TEXT })
    public String ping();

    /**
     * @see [get] http://localhost:80/hqlbuilder/rest/pojo/sqlforhql?hql=...
     */
    @GET
    @Path("/sqlforhql")
    @Produces({ TEXT })
    public abstract String getSqlForHql(@QueryParam("hql") String hql);

    /**
     * @see [get] http://localhost:80/hqlbuilder/rest/pojo/classes
     */
    @GET
    @Path("/classes")
    @Produces({ XML })
    public abstract XmlWrapper<SortedSet<String>> getClasses();

    /**
     * @see [get] http://localhost:80/hqlbuilder/rest/pojo/properties?classname=...
     */
    @GET
    @Path("/properties")
    @Produces({ XML })
    public abstract XmlWrapper<List<String>> getProperties(@QueryParam("classname") String classname);

    /**
     * @see [get] http://localhost:80/hqlbuilder/rest/pojo/connectionInfo
     */
    @GET
    @Path("/connectioninfo")
    @Produces({ TEXT })
    public abstract String getConnectionInfo();

    /**
     * @see [get] http://localhost:80/hqlbuilder/rest/pojo/project
     */
    @GET
    @Path("/project")
    @Produces({ TEXT })
    public abstract String getProject();

    /**
     * @see [get] http://localhost:80/hqlbuilder/rest/pojo/search?text=...&typeName =...&hitsPerPage=...
     */
    @GET
    @Path("/search")
    @Produces({ XML })
    public abstract XmlWrapper<List<String>> search(@QueryParam("text") String text, @QueryParam("typename") String typeName,
            @QueryParam("hitsperpage") int hitsPerPage);

    /**
     * @see [get] http://localhost:80/hqlbuilder/rest/pojo/reservedkeywords
     */
    @GET
    @Path("/reservedkeywords")
    @Produces({ XML })
    public abstract XmlWrapper<Set<String>> getReservedKeywords();

    /**
     * @see [get] http://localhost:80/hqlbuilder/rest/pojo/namedqueries
     */
    @GET
    @Path("/namedqueries")
    @Produces({ XML })
    public abstract XmlWrapper<Map<String, String>> getNamedQueries();

    /**
     * @see [get] http://localhost:80/hqlbuilder/rest/pojo/createscript
     */
    @GET
    @Path("/createscript")
    @Produces({ TEXT })
    public abstract String createScript();

    /**
     * @see [get] http://localhost:80/hqlbuilder/rest/pojo/hibernateinfo
     */
    @GET
    @Path("/hibernateinfo")
    @Produces({ XML })
    public abstract XmlWrapper<Map<String, String>> getHibernateInfo();

    /**
     * @see [get] http://localhost:80/hqlbuilder/rest/pojo/hibernatehelpurl
     */
    @GET
    @Path("/hibernatehelpurl")
    @Produces({ TEXT })
    public abstract String getHibernateHelpURL();

    /**
     * @see [get] http://localhost:80/hqlbuilder/rest/pojo/hqlhelpurl
     */
    @GET
    @Path("/hqlhelpurl")
    @Produces({ TEXT })
    public abstract String getHqlHelpURL();

    /**
     * @see [get] http://localhost:80/hqlbuilder/rest/pojo/lucenehelpurl
     */
    @GET
    @Path("/lucenehelpurl")
    @Produces({ TEXT })
    public abstract String getLuceneHelpURL();

    /**
     * @see [get] http://localhost:80/hqlbuilder/rest/pojo/propertynames?key=...& parts=...&parts=...
     */
    @GET
    @Path("/propertynames.")
    @Produces({ TEXT })
    public abstract XmlWrapper<List<String>> getPropertyNames(@QueryParam("key") String key, @QueryParam("parts") String[] parts);

    /**
     * @see [get] http://localhost:80/hqlbuilder/rest/pojo/sql?sql=...&sql=...
     */
    @GET
    @Path("/sql")
    @Produces({ TEXT })
    public abstract void sql(@QueryParam("sql") String[] sql);

    /**
     * @see [get] http://localhost:80/hqlbuilder/rest/pojo/findparameters?hql=...
     */
    @GET
    @Path("/findparameters")
    @Produces({ XML })
    @Wrapped
    public abstract XmlWrapper<List<QueryParameter>> findParameters(@QueryParam("hql") String hql);

    /**
     * @see [put] http://localhost:80/hqlbuilder/rest/pojo/save/{...} [body]
     */
    @PUT
    @Path("/save/{pojo}")
    @Consumes({ XML })
    @Produces({ XML })
    public abstract void save(@PathParam("pojo") String pojo, Object object);

    /**
     * @see [delete] http://localhost:80/hqlbuilder/rest/pojo/delete/{...} [body]
     */
    @DELETE
    @Path("/delete/{pojo}")
    @Consumes({ XML })
    public abstract void delete(@PathParam("pojo") String pojo, Object object);

    /**
     * @see [put] http://localhost:80/hqlbuilder/rest/pojo/execute [body]
     */
    @PUT
    @Path("/execute")
    @Consumes({ XML })
    @Produces({ XML })
    public abstract ExecutionResult execute(QueryParameters queryParameters);

    /**
     * @see [get] http://localhost:80/hqlbuilder/rest/pojo/hibernatewebresolver
     */
    @GET
    @Path("/hibernatewebresolver")
    @Produces({ BINARY })
    public abstract StreamingOutput getHibernateWebResolver();

    /**
     * @see [put] http://localhost:80/hqlbuilder/rest/pojo/get [type,id]
     */
    @Path("get")
    @POST
    @Produces({ XML })
    public abstract <T> XmlWrapper<T> get(@FormParam("type") String type, @FormParam("id") String id);
}
