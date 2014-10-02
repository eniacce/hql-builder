package org.tools.hqlbuilder.webservice.js;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.tools.hqlbuilder.webservice.jquery.ui.colors.Colors;
import org.tools.hqlbuilder.webservice.jquery.ui.datepicker.JQueryDatePicker;
import org.tools.hqlbuilder.webservice.jquery.ui.jqueryui.JQueryUI;
import org.tools.hqlbuilder.webservice.jquery.ui.primeui.PrimeUI;
import org.tools.hqlbuilder.webservice.jquery.ui.spectrum.Spectrum;

import ro.isdc.wro.extensions.processor.js.UglifyJsProcessor;

public class WicketJSRoot {
    public static void main(String[] args) {
        minify(JQueryDatePicker.class, "", new String[] { "JQDatePicker" });
        minify(Colors.class, "", new String[] { "colors" });
        minify(Spectrum.class, "", new String[] { "spectrum" });
        minify(PrimeUI.class, "", new String[] { "primeui-factory" });
        minify(JQueryUI.class, "", new String[] { "jquery-ui-factory" });
    }

    protected static void minify(Class<?> root, String path, String[] sources) {
        try {
            String ext = "js";
            for (String source : sources) {
                File css = new File("src/main/resources/" + root.getPackage().getName().replace('\\', '/').replace('.', '/') + path + "/" + source
                        + "." + ext);
                InputStreamReader in = new InputStreamReader(new FileInputStream(css));
                UglifyJsProcessor compressor = new UglifyJsProcessor();
                OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File("src/main/resources/"
                        + root.getPackage().getName().replace('\\', '/').replace('.', '/') + path + "/" + source + ".mini." + ext)));
                compressor.process(in, out);
                in.close();
                out.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
