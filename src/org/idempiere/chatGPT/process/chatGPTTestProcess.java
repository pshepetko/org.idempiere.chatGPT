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


import org.compiere.model.MSysConfig;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.Env;
import org.idempiere.chatGPT.base.CustomProcess;

import java.util.logging.Level;

/**  
 * Process create chatGPT Test Integration (ONLY FOR DEMO) 
 */
public class chatGPTTestProcess extends CustomProcess  {

	private String		processVerNo = "[v.2.00] ";
	private String 		p_TextMsg="";
	private String		result="";
	
	@Override
	protected void prepare() {
			ProcessInfoParameter[] para = getParameter();
			for (int i = 0; i < para.length; i++)
			{
				String name = para[i].getParameterName();
				if (para[i].getParameter() == null && para[i].getParameter_To() == null)
					;
				else if (name.equals("TextMsg"))
					p_TextMsg=  para[i].getParameterAsString();
				else
					log.log(Level.SEVERE, "Unknown Parameter: " + name);
			}
		}
	
	@Override
	protected String doIt() throws Exception {
		String		chatGPT_API_Key = MSysConfig.getValue("chatGPT_API_Key",Env.getAD_Client_ID(Env.getCtx()));
		if ((chatGPT_API_Key == null || chatGPT_API_Key.length() == 0) == true)		
		{
			result="ERROR: You didn't add chatGPT API Key to System Config!";
		} else
			{					
			result=  "Connecting to chatGPT!<br>";
			result+= "Question: " +p_TextMsg +"<br>";
			result+= "Answer: " +org.idempiere.chatGPT.process.chatGPT.chatGPTconnect(p_TextMsg); 
			}
		return processVerNo+"<br>"+result ;
	}
}
