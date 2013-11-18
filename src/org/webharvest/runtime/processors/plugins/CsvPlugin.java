package org.webharvest.runtime.processors.plugins;

import java.util.Iterator;

import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

public class CsvPlugin extends WebHarvestPlugin {
	
	@Override
	public String getName() {
		return "csv";
	}

	@Override
	public Variable executePlugin(Scraper scraper, ScraperContext context) {
        String dir = evaluateAttribute("dir", scraper);
        boolean isWriteHeader = evaluateAttributeAsBoolean("writeheader", true, scraper);
        String delimiter = evaluateAttribute("delimiter", scraper);
        String charset = evaluateAttribute("charset", scraper);
        String fileName = evaluateAttribute("filename", scraper);
        
        Variable body = executeBody(scraper, context);
        
        if(body!=null){
        	Iterator<Variable> it = body.toList().listIterator();
        	StringBuilder xmlBody = new StringBuilder();
        	xmlBody.append("<?xml version=\"1.0\"?><tempWrapper>");
        	while (it.hasNext()) {
        		Variable currElement = it.next();
        		xmlBody.append(currElement.toString());
        	}
        	xmlBody.append("</tempWrapper>");
        	CommonUtil.resultXmlToCsv(xmlBody.toString(), isWriteHeader, dir, fileName, charset, delimiter);
        }
        return new NodeVariable( "<?xml version=\"1.0\"?><wrapper>OK</wrapper>");
	}
	
	@Override
    public String[] getValidAttributes() {
        return new String[] {
                "dir",
                "writeheader",
                "delimiter",
                "charset",
                "filename"};
    }

	@Override
    public String[] getRequiredAttributes() {
        return new String[] {
        		"dir", "writeheader", "delimiter", "charset", "filename"
        		};
    }
}
