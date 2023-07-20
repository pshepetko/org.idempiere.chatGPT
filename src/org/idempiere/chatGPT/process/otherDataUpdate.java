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

package org.idempiere.chatGPT.process;


import java.util.logging.Level;

import org.compiere.model.MForm;
import org.compiere.model.MInfoWindow;
import org.compiere.model.MMenu;
import org.compiere.model.MMessage;
import org.compiere.model.MSysConfig;
import org.compiere.model.MWindow;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.Env;
import org.idempiere.chatGPT.base.CustomProcess;

/**  
 *  Process create chatGPT Test Integration (ONLY FOR DEMO) 
 */
public class otherDataUpdate extends CustomProcess  {

	private String		processVerNo = "[v.1.00] ";
	private int 		p_AD_Message_ID=0;
	private int 		p_AD_Window_ID=0;
	private int 		p_AD_Menu_ID=0;
	private int 		p_AD_Form_ID=0;
	private int 		p_AD_InfoWindow_ID=0;
	private String		p_Type="";
	private String		chatGPT_API_Key = MSysConfig.getValue("chatGPT_API_Key",Env.getAD_Client_ID(Env.getCtx()));
	private String		result="";
	
	@Override
	protected void prepare() {
			ProcessInfoParameter[] para = getParameter();
			for (int i = 0; i < para.length; i++)
			{
				String name = para[i].getParameterName();
				if (para[i].getParameter() == null && para[i].getParameter_To() == null)
					;
				else if (name.equals("Type"))
					p_Type =  para[i].getParameterAsString();
				else if (name.equals("AD_Message_ID"))
					p_AD_Message_ID =  para[i].getParameterAsInt();
				else if (name.equals("AD_Window_ID"))
					p_AD_Window_ID=  para[i].getParameterAsInt();
				else if (name.equals("AD_InfoWindow_ID"))
					p_AD_InfoWindow_ID=  para[i].getParameterAsInt();	
				else if (name.equals("AD_Form_ID"))
					p_AD_Form_ID=  para[i].getParameterAsInt();	
				else if (name.equals("AD_Menu_ID"))
					p_AD_Menu_ID=  para[i].getParameterAsInt();	
				else
					log.log(Level.SEVERE, "Unknown Parameter: " + name);
			}
		}
	
	@Override
	protected String doIt() throws Exception {
		if ((chatGPT_API_Key == null || chatGPT_API_Key.length() == 0) == true)		
		{
			result="ERROR: You didn't add chatGPT API Key to System Config for current the Client!";
		} else
			{	
			result="Connect to chatGPT!<br>";
			
			MMessage p_mess  = new MMessage (Env.getCtx(),p_AD_Message_ID,get_TrxName()); 

			String obj_name ="";
			String type_name ="";
  			String p_Answer_Description= "";
 			String p_Answer_Help= "";
 			String less200=" in iDempiere (less than 200 characters).";
 			String less2000=" in iDempiere (less than 2000 characters).";
 			
			switch (p_Type) {

		      case "W":
		      {
		    	  	type_name = "Window";
		    	  	MWindow p_wind  = new MWindow (Env.getCtx(),p_AD_Window_ID,get_TrxName()); 
		    	  	
		    	  	if (p_wind.getWindowType().equals("T") == true) type_name = "Document";
		    	  	
				  	obj_name=p_wind.getName();				  
				  	p_Answer_Description= org.idempiere.chatGPT.process.chatGPT.chatGPTconnect(
				  			p_mess.getMsgText()+" "+ type_name+" "+obj_name+less200);
 		 		  	p_Answer_Help= org.idempiere.chatGPT.process.chatGPT.chatGPTconnect(
 		 		  			p_mess.getMsgText()+" "+ type_name+" "+obj_name+less2000);				   
 		 		  	p_wind.setDescription(p_Answer_Description.trim());
				  	p_wind.setHelp(p_Answer_Help.trim());
				  	p_wind.saveEx();
				  	 result+= "Record: "+type_name+" '"+obj_name+"' updated!";
		    	  break; 
		      }
		      case "IW":
			      {
			    	  	type_name = "Info Window";
			    	  	MInfoWindow p_infowind  = new MInfoWindow (Env.getCtx(),p_AD_InfoWindow_ID,get_TrxName()); 		    	  
					  	obj_name=p_infowind.getName();				  
					  	p_Answer_Description= org.idempiere.chatGPT.process.chatGPT.chatGPTconnect(
					  			p_mess.getMsgText()+" "+ type_name+" "+obj_name+less200);
	 		 		 	p_Answer_Help= org.idempiere.chatGPT.process.chatGPT.chatGPTconnect(
	 		 		 			p_mess.getMsgText()+" "+ type_name+" "+obj_name+less2000);				  
	 		 		 	p_infowind.setDescription(p_Answer_Description.trim());
	 		 			p_infowind.setHelp(p_Answer_Help.trim());
	 		 			p_infowind.saveEx();
	 		 			 result+= "Record: "+type_name+" '"+obj_name+"' updated!";
	 		 			break; 
			      }
		      case "X":
			      {
			    	  	type_name = "Form";
			    	  	MForm p_form = new MForm (Env.getCtx(),p_AD_Form_ID,get_TrxName()); 
			    	   	obj_name=p_form.getName();				  
		 		 		 p_Answer_Description= org.idempiere.chatGPT.process.chatGPT.chatGPTconnect(
		 		 				 p_mess.getMsgText()+" "+ type_name+" "+obj_name+less200);
		 		 		 p_Answer_Help= org.idempiere.chatGPT.process.chatGPT.chatGPTconnect(
		 		 				 p_mess.getMsgText()+" "+ type_name+" "+obj_name+less2000);					  
		 		 		 p_form.setDescription(p_Answer_Description.trim());
		 		 		 p_form.setHelp(p_Answer_Help.trim());
		 		 		 p_form.saveEx();
	 		 			 result+= "Record: "+type_name+" '"+obj_name+"' updated!";
				    	 break; 
			      }
		      case "M":
		      {
		    	  	type_name = "Menu";
			    	  MMenu p_form = new MMenu (Env.getCtx(),p_AD_Menu_ID,get_TrxName()); 
			    	 obj_name=p_form.getName();		    	  
	 		 		 p_Answer_Description= org.idempiere.chatGPT.process.chatGPT.chatGPTconnect(
	 		 				 p_mess.getMsgText()+" "+ type_name+" "+obj_name+less200);					  
	 		 		 p_form.setDescription(p_Answer_Description.trim());
	 		 		 p_form.saveEx();
 		 			 result+= "Record: "+type_name+" '"+obj_name+"' updated!";
			    	 break; 
		      }  
		       default :  
					  result+= "This Type don't support!";
		    }
			}
		return processVerNo+result ;
	} 
	
}
