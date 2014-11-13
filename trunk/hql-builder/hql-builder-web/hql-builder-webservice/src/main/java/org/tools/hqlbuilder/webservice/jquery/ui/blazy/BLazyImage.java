package org.tools.hqlbuilder.webservice.jquery.ui.blazy;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

import de.agilecoders.wicket.core.markup.html.bootstrap.behavior.CssClassNameAppender;

public class BLazyImage extends Image {
    private static final long serialVersionUID = 2477527041537407931L;

    public BLazyImage(String id) {
        super(id);
        adjustImage(this);
    }

    public BLazyImage(String id, ResourceReference resourceReference) {
        super(id, resourceReference);
        adjustImage(this);
    }

    public BLazyImage(String id, IResource imageResource) {
        super(id, imageResource);
        adjustImage(this);
    }

    public BLazyImage(String id, IModel<?> model) {
        super(id, model);
        adjustImage(this);
    }

    public BLazyImage(String id, String string) {
        super(id, string);
        adjustImage(this);
    }

    public BLazyImage(String id, ResourceReference resourceReference, PageParameters resourceParameters) {
        super(id, resourceReference, resourceParameters);
        adjustImage(this);
    }

    public static Image adjustImage(Image image) {
        image.add(new CssClassNameAppender(BLazy.BLAZY_CLASS));
        return image;
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        adjustTag(tag);
    }

    public static ComponentTag adjustTag(ComponentTag tag) {
        String src = "src";
        tag.getAttributes().put(BLazy.BLAZY_SRC, tag.getAttributes().getString(src));
        tag.getAttributes().put(src, BLazy.IMAGE_PLACEHOLDER);
        return tag;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        if (!isEnabledInHierarchy()) {
            return;
        }
        adjustResponse(response);
    }

    public static IHeaderResponse adjustResponse(IHeaderResponse response) {
        response.render(JavaScriptHeaderItem.forReference(BLazy.BLAZY_JS));
        response.render(OnDomReadyHeaderItem.forScript(BLazy.BLAZY_FACTORY_JS));
        response.render(CssHeaderItem.forReference(BLazy.BLAZY_CSS));
        return response;
    }
}
