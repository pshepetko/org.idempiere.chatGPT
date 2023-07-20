package org.idempiere.chatGPT.process;
/**
 * This file is part of iDempiere ERP <http://www.idempiere.org>.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Copyright (c) 2016 Saúl Piña <sauljabin@gmail.com>.
 */

import org.compiere.model.MProduct;
import org.compiere.model.MSysConfig;
import org.compiere.util.Env;
import org.idempiere.chatGPT.base.CustomProcess;

/**  
 * Process create chatGPT Test Integration (ONLY FOR DEMO) 
 */
public class productUpdate extends CustomProcess  {

	private String		processVerNo = "[v.1.00] ";
	private int 		p_M_Product_ID=0;
	private String		chatGPT_API_Key = MSysConfig.getValue("chatGPT_API_Key",Env.getAD_Client_ID(Env.getCtx()));
	private String		result="";
	
	@Override
	protected void prepare() {
		p_M_Product_ID = getRecord_ID();
	}
	
	@Override
	protected String doIt() throws Exception {
		if ((chatGPT_API_Key == null || chatGPT_API_Key.length() == 0) == true)		
		{
			result="ERROR: You didn't add chatGPT API Key to System Config!";
		} else
			{		
			MProduct prod = new MProduct (Env.getCtx(),p_M_Product_ID,get_TrxName()); 
			String p_Question_Description = "Describe (less than 200 characters)  the "+prod.getName()+ " as "+prod.getM_Product_Category().getName();
			String p_Question_Help = 		"Describe (less than 2000 characters) the "+prod.getName()+ " as "+prod.getM_Product_Category().getName();
				
			result="Connect to chatGPT!";
			String p_Answer_Description= org.idempiere.chatGPT.process.chatGPT.chatGPTconnect(p_Question_Description);
			String p_Answer_Help= org.idempiere.chatGPT.process.chatGPT.chatGPTconnect(p_Question_Help);
			
	 		prod.setDescription(p_Answer_Description.trim());
			prod.setHelp(p_Answer_Help.trim());
			prod.saveEx();
			result+="Product <"+prod.getName()+"> updated!";
			}
		return processVerNo+result ;
	} 
	
}
