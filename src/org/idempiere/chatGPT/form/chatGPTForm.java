package org.idempiere.chatGPT.form;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;

import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.apps.form.MFARegisterForm;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.Label;
import org.adempiere.webui.component.ListModelTable;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.component.Textbox;
import org.adempiere.webui.component.WListbox;
import org.adempiere.webui.component.Window;
import org.adempiere.webui.panel.ADForm;
import org.adempiere.webui.panel.CustomForm;
import org.adempiere.webui.session.SessionManager;
import org.adempiere.webui.util.IServerPushCallback;
import org.adempiere.webui.util.ServerPushTemplate;
import org.adempiere.webui.util.ZKUpdateUtil;
import org.compiere.minigrid.IMiniTable;
import org.compiere.model.MQuery;
import org.compiere.model.MSysConfig;
import org.compiere.model.SystemIDs;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Center;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.North;
import org.zkoss.zul.Separator;
import org.zkoss.zul.South;

import org.json.JSONException;
import org.idempiere.chatGPT.base.CustomFormController;


 public class chatGPTForm  extends CustomFormController 
{
	/** UI Form instance */
	private CustomForm form = new CustomForm();

    /** Logger.          */
    private static final CLogger log = CLogger.getCLogger(chatGPTForm.class);

    // new question panel
    /** Grid for components for creating a new question to chatGPT. North of {@link #form}. */
    private Grid m_grdNew = GridFactory.newGridLayout();
    /** Name field label. */
    private Label m_lblQuestion = new Label();
    /** Field for question. */
    private Textbox m_txbQuestionField = new Textbox();
    /** Button to create new question. */
    private Button m_btnReset = new Button();

    // Answer panel
    /** Grid for components for creating answer.  */
    private Panel m_pnlAnswer = new Panel();
    /** Button to close form.   */
    private Button m_btnClose = new Button();
    /** Table to hold data of answer.  */
    private WListbox m_tblData = new WListbox();
 

	private ResultSetMetaData rsmd; 
	private int columnsNumber = 0;
	
	private String		FormVerNo = "[v.2.00] ";
	
	// Set ChatGPT endpoint and API key
	private String		chatGPT_API_Key = MSysConfig.getValue("chatGPT_API_Key",Env.getAD_Client_ID(Env.getCtx()));
	private String endpoint = "https://api.openai.com/v1/chat/completions";
	
	  // SYSTEM_TASK_MESSAGE need for training chatGPT
	  private static final String SYSTEM_TASK_MESSAGE1 = 
	          "iDempiere use table M_Product for Products"+
	          " and table C_BPartner for Business Partners"+ 
	          " and table M_ProductPrice for Price Lists "
	          + " and table C_Project for Project"
	          + " and table C_Order for Orders "
	          + " and table C_Invoice for Invoices "
	          + " and table M_InOut for Shipment and Receipt "
	          + " and table M_Storage for Stock of Product "
	          + " and table C_Region for Region "
	          + " and table C_Country for Country "
	          + " and table C_Currency for Currency "
	          + " and table AD_Form for Forms "
	          + " and table AD_Window for Windows "
	          + " and table AD_Process for Process "
	          + " and table AD_InfoWindow for InfoWindows "
	          + " Don't add anything else in the end before you respond with the JSONS."+
	          "Don't say anything else. Respond only with the SQL." +
	          "Don't add anything else in the end after you respond with the JSON.";
	  
	  private static final String SYSTEM_TASK_MESSAGE2 = 
	           "iDempiere use table AD_Form for Form."
 	          + " and table AD_Window for Window."
 	          + " and table AD_Window for Document."
 	          + " and table AD_Process for Process."
 	          + " and table AD_Process for Report."
 	          + " and table AD_InfoWindow for InfoWindow. "
	          + " Don't add anything else in the end before you respond with the JSONS."+
	          " Don't say anything else."
	          + " Respond only with the SQL. Use LOWER in SQL." + 
	          "Don't add anything else in the end after you respond with the JSON.";
	
    /**
     * Default constructor.
     */
    public chatGPTForm()
    {
        super();
        initForm();
    }

    /**
     * Initialises the panel.
     */
    protected void initForm()
    {
        if (log.isLoggable(Level.INFO)) log.info("");
        try
        {
            staticInitialise();
            zkInit();
        }
        catch(Exception e)
        {
            log.log(Level.SEVERE, "", e);
        }
    }

    /**
     * Initialises the static components of the form.
     */
    private void staticInitialise()
    {
        createQuestionPanel();
        createAnswerPanel();
    }

    /**
     * Layout
     */
    private void zkInit()
	{
		Borderlayout contentPane = new Borderlayout();
		form.appendChild(contentPane);

		North north = new North();
		contentPane.appendChild(north);
		north.appendChild(m_grdNew);
		north.setCollapsible(true);
		north.setSplittable(true);

		Center center = new Center();
        contentPane.appendChild(center);
		center.appendChild(m_pnlAnswer);
	}

    /**
     * Creates Answer panel.
     */
	private void createAnswerPanel()
    {
    	Borderlayout borderlayout = new Borderlayout();
    	borderlayout.setStyle("position: relative");
    	ZKUpdateUtil.setWidth(borderlayout, "100%");
    	ZKUpdateUtil.setHeight(borderlayout, "100%");
    	m_pnlAnswer.appendChild(borderlayout);
    	ZKUpdateUtil.setWidth(m_pnlAnswer, "100%");
    	ZKUpdateUtil.setHeight(m_pnlAnswer, "100%");

		North north = new North();
		north.setBorder("none");
		borderlayout.appendChild(north);
        Label label = new Label("Result from chatGPT");

        label.setStyle("font-weight: bold;");
		north.appendChild(label);

		Center center = new Center();
		center.setBorder("none");
		center.setAutoscroll(true);
		borderlayout.appendChild(center);
		center.appendChild(m_tblData);
		ZKUpdateUtil.setVflex(m_tblData, "1");
		ZKUpdateUtil.setHflex(m_tblData, "1");

		South south = new South();
		south.setBorder("none");
		borderlayout.appendChild(south);
		Panel southPanel = new Panel();
		southPanel.setStyle("text-align: right;");
		south.appendChild(southPanel);
		m_btnClose.setLabel("Close");
		m_btnClose.addEventListener(Events.ON_CLICK, this);
        southPanel.appendChild(new Separator());
		southPanel.appendChild(m_btnClose);
    }

    /**
     * Create the New Question panel .
     */
	private void createQuestionPanel()
    {
        final int nameFieldColumns = 20;

        // bottom row
        m_lblQuestion.setValue("Message");
        m_txbQuestionField.setCols(nameFieldColumns);
        m_txbQuestionField.addEventListener(Events.ON_CHANGE, this);
        m_btnReset.setLabel("Reset");
        m_btnReset.addEventListener(Events.ON_CLICK, this);

    	Rows rows = new Rows();
    	m_grdNew.appendChild(rows);

    	Row row = new Row();
        rows.appendChild(row);
        Label label = new Label(FormVerNo+"This Form under reconstruction!");//"Enter a question for the chatGPT.");
        label.setStyle("font-weight: bold;");
        row.appendCellChild(label, 3);  
  //reconstruction    
 /*     	row = new Row();
        rows.appendChild(row);
        Label label1 = new Label("For example:");
        label1.setStyle("font-weight: normal;");
        row.appendCellChild(label1, 3);  
        
      	row = new Row();
        rows.appendChild(row);
        Label label2 = new Label("1. Create report: 1.1 Create the report about Name of Products; 1.2 Create the report about Invoice of Business Partners");
        label2.setStyle("font-weight: normal;");
        row.appendCellChild(label2, 3);  
        
      	row = new Row();
        rows.appendChild(row);
        Label label3 = new Label("2.Open PO like Windows,Forms,InfoWindows, and Proceses : 2.1 Open  the document called Sales Order; 2.2 Open the infowindow called Order Info; 2.3 Open the process called Auto Allocation .");
        label3.setStyle("font-weight: normal;");
        row.appendCellChild(label3, 3);  
  */      
       
        
        row = new Row();
        rows.appendChild(row);
        row.appendChild(m_lblQuestion.rightAlign());
        row.appendChild(m_txbQuestionField);
        row.appendChild(m_btnReset);
        ZKUpdateUtil.setHflex(m_txbQuestionField, "1");
        
    	row = new Row();
        rows.appendChild(row);

        row = new Row();
        rows.appendChild(row);
        row.appendCellChild(new Separator(), 3);
   }
 

    /**
     *  Update results of the form.
     * @throws Exception 
     */
    private void updateResults(String sql) throws Exception
    {    	        
    	ListModelTable model = new ListModelTable(getData(sql));
        m_tblData.setData(model, getColumnNames());
		setColumnClass(m_tblData);			
        return;
    }    
    

	public Vector<String> getColumnNames() throws SQLException
	{
		//  Header Info
		Vector<String> columnNames = new Vector<String>(columnsNumber);
		columnNames.add("Key");	
		for(int i = 1; i<=columnsNumber; i++) {		
 			columnNames.add(Msg.translate(Env.getCtx(), rsmd.getColumnName(i)));
	    }
		return columnNames;
	}
    
	public void setColumnClass(IMiniTable dataTable)
	{
		//  Table UI
		for(int i = 1; i<=columnsNumber; i++) {		
			dataTable.setColumnClass(i, String .class, true);				
	    }
		dataTable.autoSize();
	}
    
    /**
     *  Event Listener.
     *
     *  @param event event that has been fired.
     * @throws Exception 
     */
    public void onEvent(Event event) throws Exception
    {
        if (log.isLoggable(Level.INFO)) log.info(event.getName());
        //  new Question
        else if (event.getTarget().equals(m_btnReset))
        {       	
        	 //reconstruction 
        	//m_txbQuestionField.setValue("");
        	//m_tblData.clearTable();
        	//m_txbQuestionField.setFocus(true);
			Messagebox.show( "This Form under reconstruction!", "Info",Messagebox.OK, Messagebox.INFORMATION);
        	
        }
        else if (event.getTarget().equals(m_txbQuestionField))
        {
        	//chatGPTconnect(); //reconstruction 
        	Messagebox.show( "This Form under reconstruction!", "Info",Messagebox.OK, Messagebox.INFORMATION);
        }
        
        else if (event.getTarget().equals(m_btnClose))
        {
        	 close();
        }
    }


    public void chatGPTconnect() throws Exception 
    {
         
		if ((chatGPT_API_Key == null || chatGPT_API_Key.length() == 0) == true)		
		{
			Messagebox.show( "You didn't add chatGPT API Key to System Config!", "Error",Messagebox.OK, Messagebox.ERROR);
		} else
			{
				String object_id="";
				String  TextMsg= m_txbQuestionField.getValue().replaceAll("Create the report", "Create the query");
			  
			  if (TextMsg.toLowerCase().contains("form"))
				  object_id= "AD_Form_ID";
			  else if (TextMsg.toLowerCase().contains("infowindow")||TextMsg.toLowerCase().contains("info window"))
				  object_id= "AD_InfoWindow_ID";
			  else if (TextMsg.toLowerCase().contains("window")||TextMsg.toLowerCase().contains("document"))
				  object_id= "AD_Window_ID";
			  else if (TextMsg.toLowerCase().contains("report") || TextMsg.toLowerCase().contains("process") )
				  object_id= "AD_Process_ID";			  
			  else 
				  object_id= "notype";
			
			  
			    try
			    {
			    	String response ="";
			      // Send input to ChatGPT API and receive response			    	
			     response = ChatGPT.sendQuery(TextMsg, endpoint, chatGPT_API_Key,SYSTEM_TASK_MESSAGE2);
			      
				    if (response.substring(0,6).equalsIgnoreCase("SELECT")) 
				    {				    	
				    	//update table of form
				    		updateResults(response);
				    		
				    		// open PO
				    		if (object_id.equals("notype") == false)
				    			openPO(response,object_id);
				    		
				    		log.log(Level.INFO,"Response from chatGPT: ", response);
				    }
				    else
				    {
				    	Messagebox.show( response, "Info",Messagebox.OK, Messagebox.INFORMATION);
				    }	
 			      log.log(Level.INFO,"Response: {}", response);
			    } catch (JSONException e) {
			    	log.log(Level.INFO, "Error parsing API response: {}", e.getMessage());
			    } catch (Exception e) {
 			      log.log(Level.INFO,"Unexpected error: {}", e.getMessage());
			    }
			  
			}
    }
    
    
    public void openPO(String resp,String object_id) throws Exception 
    {
    	int po_id = DB.getSQLValue(null, "SELECT "+object_id+" FROM ("+resp.replace(";", "")+") AS query;");
    	if (po_id >0)
    		
    		switch (object_id) {
    		  case "AD_Form_ID":
    	    		SessionManager.getAppDesktop().openForm(po_id);
    		    break;
    		  case "AD_InfoWindow_ID":
  	    		SessionManager.getAppDesktop().openInfo(po_id);
  	    		break;
    		  case "AD_Window_ID":
  	    		SessionManager.getAppDesktop().openWindow(po_id,null);
  	    		break;    		
    		  case "AD_Process_ID":
  	    		SessionManager.getAppDesktop().openProcessDialog(po_id,false);
  	    		break;
    		  default: Messagebox.show( "ERROR OPEN PO : "+object_id+"="+po_id, "Info",Messagebox.OK, Messagebox.INFORMATION);

    		}
    }
    
  public Vector<Vector<Object>> getData(String sql)
    		{
    			int count=1;
	  			//  Table
    			Vector<Vector<Object>> data = new Vector<Vector<Object>>();

    			PreparedStatement pstmt = null;
    			ResultSet rs = null;
    			try
    			{
    				pstmt = DB.prepareStatement(sql, null);
    				rs = pstmt.executeQuery();
    				
    				//get columnsNumber
    				rsmd = rs.getMetaData();
    				columnsNumber = rsmd.getColumnCount();
    				
    				while (rs.next())
    				{
    					Vector<Object> line = new Vector<Object>(columnsNumber);
    					KeyNamePair pp = new KeyNamePair(count,Integer.toString(count));
    					
    					for(int i = 1; i <= columnsNumber; i++)
    					{
    						if (i==1)
    							line.add(pp); 
    						 
    						if(rs.getObject(i) != null) 	
    							line.add(rs.getObject(i).toString());
    						else
    							line.add("");
    					}
    					
    					data.add(line); count++;
    				}
    			}
    			catch (SQLException e)
    			{
    				log.log(Level.SEVERE, sql, e);
    			}
    			finally
    			{
    				DB.close(rs, pstmt);
    				rs = null;
    				pstmt = null;
    			}    			
    			return data;
    		}
  
    /**
     * Close form.
     */
    public void close()
    {
        SessionManager.getAppDesktop().closeActiveWindow();
    }  
    
    public ADForm getForm() {
		return form;
	}

	@Override
	protected void buildForm() throws Exception {
		// TODO Auto-generated method stub
		
	}
	


}


