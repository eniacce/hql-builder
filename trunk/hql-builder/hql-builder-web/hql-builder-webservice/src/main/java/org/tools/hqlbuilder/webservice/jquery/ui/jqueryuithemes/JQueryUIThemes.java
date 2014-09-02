package org.tools.hqlbuilder.webservice.jquery.ui.jqueryuithemes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.tools.hqlbuilder.webservice.wicket.CssResourceReference;

public class JQueryUIThemes {
    public static CssResourceReference base(String theme) {
        return new CssResourceReference(JQueryUIThemes.class, theme + "/jquery.ui.theme.css");
    }

    public static CssResourceReference theme(String theme) {
        return new CssResourceReference(JQueryUIThemes.class, theme + "/jquery.ui.css").addCssResourceReferenceDependency(base(theme));
    }

    protected static List<String> themes = null;

    public static List<String> getThemes() {
        if (themes == null) {
            themes = new ArrayList<String>();
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(JQueryUIThemes.class.getClassLoader().getResourceAsStream(
                        JQueryUIThemes.class.getPackage().getName().replace('\\', '/').replace('.', '/') + "/themes.list")));
                String theme;
                while ((theme = in.readLine()) != null) {
                    themes.add(theme);
                }
                in.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return themes;
    }
}
