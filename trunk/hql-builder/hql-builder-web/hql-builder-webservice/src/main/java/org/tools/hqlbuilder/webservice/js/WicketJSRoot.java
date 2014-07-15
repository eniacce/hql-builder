package org.tools.hqlbuilder.webservice.js;

import org.apache.wicket.request.resource.ResourceReference;
import org.tools.hqlbuilder.webservice.wicket.JavaScriptResourceReference;
import org.tools.hqlbuilder.webservice.wicket.WicketApplication;

import com.googlecode.wicket.jquery.core.settings.IJQueryLibrarySettings;

public class WicketJSRoot {
    public static JavaScriptResourceReference FLOATING_BAR = new JavaScriptResourceReference(WicketJSRoot.class, "floatingbar.js");

    static {
        try {
            ResourceReference jQueryUIReference = ((IJQueryLibrarySettings) WicketApplication.get().getJavaScriptLibrarySettings())
                    .getJQueryUIReference();
            FLOATING_BAR.addJavaScriptResourceReferenceDependency(jQueryUIReference);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}