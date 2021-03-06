package org.webharvest.runtime.processors.plugins;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.webharvest.exception.PluginException;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

public class TaobaoPlugin extends WebHarvestPlugin {

	@Override
	public String getName() {
		return "taobao";
	}

	@Override
	public Variable executePlugin(Scraper scraper, ScraperContext context) {
        String dir = evaluateAttribute("dir", scraper);
        boolean isWriteHeader = evaluateAttributeAsBoolean("writeHeader", true, scraper);
        String delimiter = evaluateAttribute("delimiter", scraper);
        String charset = evaluateAttribute("charset", scraper);
        
        Variable body = executeBody(scraper, context);
        List<String> headerNames = new ArrayList<String>();
        List<String> lineValues = new ArrayList<String>();
        String fileName = "", fullPath = "";
        final String newLine = System.getProperty("line.separator");
        try {
            JSONObject jsonObject = new JSONObject(body.toString());
            JSONArray itemAry = (JSONArray) jsonObject.get("itemList");
            for (int i=0;i<itemAry.length();i++) {
            	JSONObject item = itemAry.getJSONObject(i);
            	lineValues = new ArrayList<String>();
        		for (String key : JSONObject.getNames(item)){
        			if (i==0 
        					&& isWriteHeader
        					&& !headerNames.contains(key)) {
        				headerNames.add(key);
        			}
        			lineValues.add(StringEscapeUtils.escapeCsv(item.getString(key)));
        		}
            	
            	// CSV
            	fileName = item.getString("itemId") + ".csv";
            	fullPath = dir + fileName;
            	CommonUtil.writeCsv(fileName, fullPath, charset, newLine
            			, StringUtils.join(headerNames.toArray(), delimiter), StringUtils.join(lineValues.toArray(), delimiter));
            }
//            return new NodeVariable("");
            return new NodeVariable( "<?xml version=\"1.0\"?>" + "<wrapper>" + XML.toString(itemAry) + "</wrapper>");
        } catch (JSONException e) {
            throw new PluginException("Error converting JSON to XML: " + e.getMessage());
        }
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
        return new String[] {"dir", "writeHeader", "delimiter", "charset"};
    }
    
}
