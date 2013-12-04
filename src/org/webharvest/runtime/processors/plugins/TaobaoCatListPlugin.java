package org.webharvest.runtime.processors.plugins;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webharvest.exception.PluginException;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

public class TaobaoCatListPlugin extends WebHarvestPlugin {

	@Override
	public String getName() {
		return "taobaocat";
	}

	@Override
	public Variable executePlugin(Scraper scraper, ScraperContext context) {
        String dir = evaluateAttribute("dir", scraper);
        boolean isWriteHeader = evaluateAttributeAsBoolean("writeHeader", true, scraper);
        String delimiter = evaluateAttribute("delimiter", scraper);
        String charset = evaluateAttribute("charset", scraper);

        Variable body = executeBody(scraper, context);
        if(body==null){
        	return new NodeVariable( "<?xml version=\"1.0\"?><wrapper>NG</wrapper>");
        }

        Iterator<Variable> it = body.toList().listIterator();
        List<String> headerNames = new ArrayList<String>();
        headerNames.add("name");
        headerNames.add("value");
        List<String> lines = new ArrayList<String>();
        List<String> values = new ArrayList<String>();
        final String newLine = System.getProperty("line.separator");
        while (it.hasNext()) {
        	Variable currElement = it.next();
        	try {
        		JSONObject jsonObject = new JSONObject(currElement.toString());
        		Object c = jsonObject.get("cat");
        		if (c == null || !(c instanceof JSONObject)) {
        			String a = "";
        			continue;
        		}
        		Object cg = ((JSONObject)c).get("catGroupList");
        		if(cg == null || !(cg instanceof JSONArray)){
        			String a = "";
        			continue;
        		}
        		
        		// main cat
        		JSONArray mainCatAry = (JSONArray) ((JSONObject)jsonObject.get("crumb")).get("catPathList");
        		for (int i=0;i<mainCatAry.length();i++) {
        			JSONObject item = mainCatAry.getJSONObject(i);
					values = new ArrayList<String>();
					for (String key : JSONObject.getNames(item)){
						if(!headerNames.contains(key)) continue;
						values.add(StringEscapeUtils.escapeCsv(item.getString(key)));
					}
					values.add("1");
					String val = StringUtils.join(values.toArray(), delimiter);
					if (lines.contains(val)) continue;
					lines.add(val);
        		}
        		
        		// sub cat
				JSONArray itemAry = (JSONArray) cg;
				for (int i=0;i<itemAry.length();i++) {
					JSONObject item = itemAry.getJSONObject(i);
					Object o = item.get("catList");
					if(o == null) continue;
					JSONArray catAry = (JSONArray)o;
					for (int j=0;j<catAry.length();j++) {
						JSONObject cat = catAry.getJSONObject(j);
						values = new ArrayList<String>();
						for (String key : JSONObject.getNames(cat)){
							if(!headerNames.contains(key)) continue;
							values.add(StringEscapeUtils.escapeCsv(cat.getString(key)));
						}
						values.add("0");
						
						String val = StringUtils.join(values.toArray(), delimiter);
						if (lines.contains(val)) continue;
						lines.add(val);
					}
				}
        	} catch (JSONException e) {
        		throw new PluginException("Error converting JSON to XML: " + e.getMessage());
        	}
		}
        // CSV
        String fileName = System.currentTimeMillis() + ".csv";
        String fullPath = dir + fileName;
        headerNames.add("mySubCategoryFlg");
        CommonUtil.writeCsv(fileName, fullPath, charset, newLine
        		, StringUtils.join(headerNames.toArray(), delimiter), lines);
        return new NodeVariable( "<?xml version=\"1.0\"?><wrapper>OK</wrapper>");
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
