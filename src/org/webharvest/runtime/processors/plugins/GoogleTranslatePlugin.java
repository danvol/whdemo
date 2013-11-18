package org.webharvest.runtime.processors.plugins;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;

public class GoogleTranslatePlugin extends WebHarvestPlugin {

	@Override
	public String getName() {
		return "google-tran";
	}

	@Override
	public Variable executePlugin(Scraper scraper, ScraperContext context) {
        String dir = evaluateAttribute("dir", scraper);
        boolean isWriteHeader = evaluateAttributeAsBoolean("writeHeader", true, scraper);
        String delimiter = evaluateAttribute("delimiter", scraper);
        String charset = evaluateAttribute("charset", scraper);
        
        Variable body = executeBody(scraper, context);
        String httpRes = body.toString();
        String zhcn = httpRes.split("\",\"")[0].replaceAll("\\[\\[\\[\"", "").toLowerCase().trim().replaceAll(" ", "").replaceAll("Åì", "%");
        String jp = httpRes.split("\",\"")[1];
        try {
			jp = URLDecoder.decode(jp, "UTF-8");
			zhcn = URLDecoder.decode(zhcn, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
		}
        return new NodeVariable( "<?xml version=\"1.0\"?>" + "<wrapper>OK</wrapper>");
	}
	
	@Override
    public String[] getValidAttributes() {
        return new String[] {
                "dir",
                "writeHeader",
                "delimiter",
                "charset"};
    }

	@Override
    public String[] getRequiredAttributes() {
        return new String[] {
//		"dir", "writeHeader", "delimiter", "charset"
		};
    }
}
