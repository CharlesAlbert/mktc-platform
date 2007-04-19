package org.marketcetera.photon.views;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;
import org.marketcetera.core.MarketceteraException;
import org.marketcetera.photon.EclipseUtils;
import org.marketcetera.photon.PhotonPlugin;
import org.marketcetera.photon.parser.SideImage;
import org.marketcetera.photon.parser.TimeInForceImage;
import org.marketcetera.photon.preferences.CustomOrderFieldPage;
import org.marketcetera.photon.preferences.MapEditorUtil;
import org.marketcetera.photon.ui.BookComposite;
import org.marketcetera.photon.ui.validation.ControlDecoration;
import org.marketcetera.photon.ui.validation.IMessageDisplayer;
import org.marketcetera.quickfix.FIXDataDictionaryManager;
import org.marketcetera.quickfix.FIXMessageUtil;

import quickfix.DataDictionary;
import quickfix.Message;
import quickfix.field.TimeInForce;
import ca.odell.glazedlists.EventList;

public class StockOrderTicket extends ViewPart implements IMessageDisplayer, IPropertyChangeListener, IStockOrderTicket {

	private static final String CONTROL_DECORATOR_KEY = "CONTROL_DECORATOR_KEY";
	
	private static final String NEW_EQUITY_ORDER = "New Equity Order";

	private static final String REPLACE_EQUITY_ORDER = "Replace Equity Order";

	public static String ID = "org.marketcetera.photon.views.StockOrderTicket";

	private Composite top = null;

	private FormToolkit formToolkit = null; // @jve:decl-index=0:visual-constraint=""

	private ScrolledForm form = null;

	private Label errorMessageLabel = null;

	private Label sideLabel = null;

	private Label quantityLabel = null;

	private Label symbolLabel = null;

	private Label priceLabel = null;

	private Label tifLabel = null;

	private Composite sideBorderComposite = null;

	private CCombo sideCCombo = null;

	private Composite quantityBorderComposite = null;

	private Composite symbolBorderComposite = null;

	private Composite priceBorderComposite = null;

	private Composite tifBorderComposite = null;

	private Text quantityText = null;

	private Text symbolText = null;

	private Text priceText = null;

	private CCombo tifCCombo = null;

	private ExpandableComposite customFieldsExpandableComposite = null;

	private Composite customFieldsComposite = null;

	private Table customFieldsTable = null;

	private CheckboxTableViewer tableViewer = null;

	private Button sendButton;

	private Button cancelButton;

	private BookComposite bookComposite;

	private Section otherExpandableComposite;

	private Text accountText;

	private Section bookSection;

	private IMemento viewStateMemento;
	
	private static final String CUSTOM_FIELD_VIEW_SAVED_STATE_KEY_PREFIX = "CUSTOM_FIELD_CHECKED_STATE_OF_";

	private Image errorImage;

	private Image warningImage;

	private List<Control> inputControls = new LinkedList<Control>();


	public StockOrderTicket() {
		
	}

	@Override
	public void createPartControl(Composite parent) {
        FieldDecoration deco = FieldDecorationRegistry.getDefault().getFieldDecoration(
                FieldDecorationRegistry.DEC_ERROR);
        errorImage = deco.getImage();
        deco = FieldDecorationRegistry.getDefault().getFieldDecoration(
                FieldDecorationRegistry.DEC_WARNING);
        warningImage = deco.getImage();
        
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.END;
		top = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		top.setLayout(gridLayout);

		createForm();
		errorMessageLabel = getFormToolkit().createLabel(top, "");
		errorMessageLabel.setLayoutData(gridData);
		top.setBackground(errorMessageLabel.getBackground());

		PhotonPlugin plugin = PhotonPlugin.getDefault();
		plugin.getPreferenceStore().addPropertyChangeListener(this);
		

	}

	@Override
	public void dispose() {
		PhotonPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
	}

	@Override
	public void setFocus() {
	}

	/**
	 * This method initializes formToolkit
	 * 
	 * @return org.eclipse.ui.forms.widgets.FormToolkit
	 */
	private FormToolkit getFormToolkit() {
		if (formToolkit == null) {
			formToolkit = new FormToolkit(Display.getCurrent());
		}
		return formToolkit;
	}

	/**
	 * This method initializes form
	 * 
	 */
	private void createForm() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 5;
		gridLayout.marginWidth = 6;
		gridLayout.verticalSpacing = 1;
		// The horizontalSpacing needs to be wide enough to show the error image in ControlDecoration.
		gridLayout.horizontalSpacing = 6;
		gridLayout.marginHeight = 1;
		form = getFormToolkit().createScrolledForm(top);
		form.setText(NEW_EQUITY_ORDER);
		form.getBody().setLayout(gridLayout);
		sideLabel = getFormToolkit().createLabel(form.getBody(), "Side");
		quantityLabel = getFormToolkit()
				.createLabel(form.getBody(), "Quantity");
		symbolLabel = getFormToolkit().createLabel(form.getBody(), "Symbol");
		priceLabel = getFormToolkit().createLabel(form.getBody(), "Price");
		tifLabel = getFormToolkit().createLabel(form.getBody(), "TIF");
		createSideBorderComposite();
		createQuantityBorderComposite();
		createSymbolBorderComposite();
		createPriceBorderComposite();
		createTifBorderComposite();
		// createCustomFieldsExpandableComposite();
		GridData formGridData = new GridData();
		formGridData.grabExcessHorizontalSpace = true;
		formGridData.horizontalAlignment = GridData.FILL;
		formGridData.grabExcessVerticalSpace = true;
		formGridData.verticalAlignment = GridData.FILL;
		form.setLayoutData(formGridData);

		Composite okCancelComposite = getFormToolkit().createComposite(
				form.getBody());
		okCancelComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.horizontalSpan = 5;
		okCancelComposite.setLayoutData(gd);
		sendButton = getFormToolkit().createButton(okCancelComposite, "Send",
				SWT.PUSH);
		sendButton.setEnabled(false);
		cancelButton = getFormToolkit().createButton(okCancelComposite,
				"Cancel", SWT.PUSH);

		createCustomFieldsExpandableComposite();
		createOtherExpandableComposite();
		createBookComposite();

		updateCustomFields(PhotonPlugin.getDefault().getPreferenceStore()
				.getString(CustomOrderFieldPage.CUSTOM_FIELDS_PREFERENCE));
		restoreCustomFieldStates();
	}

	private void restoreCustomFieldStates() {
		if (viewStateMemento == null)
			return;
		
		TableItem[] items = customFieldsTable.getItems();
		for (int i = 0; i < items.length; i++) {
			TableItem item = items[i];
			String key = CUSTOM_FIELD_VIEW_SAVED_STATE_KEY_PREFIX + item.getText(1);
			if (viewStateMemento.getInteger(key) != null) {
				boolean itemChecked = (viewStateMemento.getInteger(key).intValue() != 0);
				item.setChecked(itemChecked);
			}
		}
	}

	/**
	 * This method initializes sideBorderComposite
	 * 
	 */
	private void createSideBorderComposite() {
		sideBorderComposite = getFormToolkit().createComposite(form.getBody());
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 2;
		gridLayout.verticalSpacing = 2;
		gridLayout.horizontalSpacing = 2;
		gridLayout.marginHeight = 2;
		sideBorderComposite.setLayout(gridLayout);
		sideCCombo = new CCombo(sideBorderComposite, SWT.BORDER);
		sideCCombo.add(SideImage.BUY.getImage());
		sideCCombo.add(SideImage.SELL.getImage());
		sideCCombo.add(SideImage.SELL_SHORT.getImage());
		sideCCombo.add(SideImage.SELL_SHORT_EXEMPT.getImage());
        addInputControl(sideCCombo);

	}

	private void addInputControl(Control control) {
		ControlDecoration cd = new ControlDecoration(control, SWT.LEFT | SWT.BOTTOM);
        cd.setMarginWidth(2);
		cd.setImage(errorImage);
		cd.hide();
		control.setData(CONTROL_DECORATOR_KEY, cd);
		inputControls.add(control);
	}

	/**
	 * This method initializes quantityBorderComposite
	 * 
	 */
	private void createQuantityBorderComposite() {
		quantityBorderComposite = getFormToolkit().createComposite(
				form.getBody());
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 2;
		gridLayout.verticalSpacing = 2;
		gridLayout.horizontalSpacing = 2;
		gridLayout.marginHeight = 2;
		quantityBorderComposite.setLayout(gridLayout);
		quantityText = getFormToolkit().createText(quantityBorderComposite,
				null, SWT.SINGLE | SWT.BORDER);
		
		Point sizeHint = EclipseUtils.getTextAreaSize(quantityBorderComposite, null, 10, 1.0);

		GridData quantityTextGridData = new GridData();
		//quantityTextGridData.heightHint = sizeHint.y;
		quantityTextGridData.widthHint = sizeHint.x;
		quantityText.setLayoutData(quantityTextGridData);
		
		quantityText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				((Text) e.widget).selectAll();
			}
		});
		addInputControl(quantityText);
	}

	/**
	 * This method initializes symbolBorderComposite
	 * 
	 */
	private void createSymbolBorderComposite() {
		GridData symbolTextGridData = new GridData();
		symbolTextGridData.horizontalAlignment = GridData.FILL;
		symbolTextGridData.grabExcessHorizontalSpace = true;
		symbolTextGridData.verticalAlignment = GridData.CENTER;
		GridData symbolBorderGridData = new GridData();
		symbolBorderGridData.horizontalAlignment = GridData.FILL;
		symbolBorderGridData.grabExcessHorizontalSpace = true;
		symbolBorderGridData.verticalAlignment = GridData.CENTER;
		symbolBorderComposite = getFormToolkit()
				.createComposite(form.getBody());
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 2;
		gridLayout.verticalSpacing = 2;
		gridLayout.horizontalSpacing = 2;
		gridLayout.marginHeight = 2;
		symbolBorderComposite.setLayout(gridLayout);
		symbolBorderComposite.setLayoutData(symbolBorderGridData);
		symbolText = getFormToolkit().createText(symbolBorderComposite, null,
				SWT.SINGLE | SWT.BORDER);
		symbolText.setLayoutData(symbolTextGridData);
		addInputControl(symbolText);
	}


	/**
	 * This method initializes priceBorderComposite
	 * 
	 */
	private void createPriceBorderComposite() {
		priceBorderComposite = getFormToolkit().createComposite(form.getBody());
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 2;
		gridLayout.verticalSpacing = 2;
		gridLayout.horizontalSpacing = 2;
		gridLayout.marginHeight = 2;
		priceBorderComposite.setLayout(gridLayout);
		priceText = getFormToolkit().createText(priceBorderComposite, null,
				SWT.SINGLE | SWT.BORDER);
		
		Point sizeHint = EclipseUtils.getTextAreaSize(priceBorderComposite, null, 10, 1.0);

		GridData quantityTextGridData = new GridData();
		//quantityTextGridData.heightHint = sizeHint.y;
		quantityTextGridData.widthHint = sizeHint.x;
		priceText.setLayoutData(quantityTextGridData);

		priceText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				((Text) e.widget).selectAll();
			}
		});
		addInputControl(priceText);
	}

	/**
	 * This method initializes tifBorderComposite
	 * 
	 */
	private void createTifBorderComposite() {
		tifBorderComposite = getFormToolkit().createComposite(form.getBody());
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 2;
		gridLayout.verticalSpacing = 2;
		gridLayout.horizontalSpacing = 2;
		gridLayout.marginHeight = 2;
		tifBorderComposite.setLayout(gridLayout);
		tifCCombo = new CCombo(tifBorderComposite, SWT.BORDER);
		tifCCombo.add(TimeInForceImage.DAY.getImage());
		tifCCombo.add(TimeInForceImage.OPG.getImage());
		tifCCombo.add(TimeInForceImage.CLO.getImage());
		tifCCombo.add(TimeInForceImage.FOK.getImage());
		tifCCombo.add(TimeInForceImage.GTC.getImage());
		tifCCombo.add(TimeInForceImage.IOC.getImage());

		addInputControl(tifCCombo);		
	}

	/**
	 * This method initializes customFieldsExpandableComposite
	 * 
	 */
	private void createCustomFieldsExpandableComposite() {
		GridData gridData3 = new GridData();
		gridData3.horizontalSpan = 3;
		gridData3.verticalAlignment = GridData.BEGINNING;
		gridData3.grabExcessHorizontalSpace = true;
		gridData3.horizontalAlignment = GridData.FILL;
		customFieldsExpandableComposite = getFormToolkit()
				.createSection(
						form.getBody(),
						Section.TITLE_BAR|
						  Section.TWISTIE);
		customFieldsExpandableComposite.setText("Custom Fields");
		customFieldsExpandableComposite.setExpanded(false);
		customFieldsExpandableComposite.setLayoutData(gridData3);
		customFieldsExpandableComposite
			.addExpansionListener(new ExpansionAdapter() {
				@Override
				public void expansionStateChanging(ExpansionEvent e) {
									form.reflow(true);
								}
				public void expansionStateChanged(ExpansionEvent e) {
					form.reflow(true);
				}
			});


		createCustomFieldsComposite();
	}

	/**
	 * This method initializes customFieldsComposite
	 * 
	 */
	private void createCustomFieldsComposite() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginWidth = 1;
		gridLayout.verticalSpacing = 1;
		gridLayout.horizontalSpacing = 1;
		gridLayout.marginHeight = 1;

		customFieldsComposite = getFormToolkit().createComposite(
				customFieldsExpandableComposite);
		customFieldsComposite.setLayout(gridLayout);
		GridData tableGridData = new GridData();
		tableGridData.verticalAlignment = GridData.CENTER;
		tableGridData.grabExcessHorizontalSpace = true;
		tableGridData.horizontalAlignment = GridData.FILL;

		customFieldsTable = new Table(customFieldsComposite, SWT.BORDER
				| SWT.CHECK | SWT.FULL_SELECTION);
		customFieldsTable.setLayoutData(tableGridData);
		customFieldsTable.setHeaderVisible(true);

		TableColumn enabledColumn = new TableColumn(customFieldsTable, SWT.LEFT);
		enabledColumn.setText("Enabled");
		enabledColumn.pack();
		TableColumn keyColumn = new TableColumn(customFieldsTable, SWT.LEFT);
		keyColumn.setText("Key");
		keyColumn.pack();
		TableColumn valueColumn = new TableColumn(customFieldsTable, SWT.LEFT);
		valueColumn.setText("Value");
		valueColumn.pack();

		// tableViewer = new CheckboxTableViewer(
		// customFieldsTable);
		// tableViewer.setContentProvider(new
		// MapEntryContentProvider(tableViewer, mapEntryList));
		// tableViewer.setLabelProvider(new MapEntryLabelProvider());
		// tableViewer.setInput(mapEntryList);

		customFieldsExpandableComposite.setClient(customFieldsComposite);

	}
	/**
	 * This method initializes customFieldsExpandableComposite
	 * 
	 */
	private void createOtherExpandableComposite() {
		GridData gridData3 = new GridData();
		gridData3.horizontalSpan = 2;
		gridData3.verticalAlignment = GridData.BEGINNING;
//		gridData3.grabExcessHorizontalSpace = true;
		gridData3.horizontalAlignment = GridData.FILL;
		otherExpandableComposite = getFormToolkit()
				.createSection(
						form.getBody(),
						Section.TITLE_BAR|
						  Section.TWISTIE);
		otherExpandableComposite.setText("Other");
		otherExpandableComposite.setExpanded(false);
		otherExpandableComposite.setLayoutData(gridData3);

		Composite otherComposite = getFormToolkit().createComposite(otherExpandableComposite);
		otherComposite.setLayout(new RowLayout(SWT.HORIZONTAL));
		Label accountLabel = getFormToolkit().createLabel(otherComposite, "Account:");
		accountText = getFormToolkit().createText(otherComposite, "");
		Point sizeHint = EclipseUtils.getTextAreaSize(quantityBorderComposite, null, 10, 1.0); 
	 	
		RowData accountTextRowData = new RowData(); 
	 	accountTextRowData.height = sizeHint.y; 
	 	accountTextRowData.width = sizeHint.x; 
	 	accountText.setLayoutData(accountTextRowData); 
	 	addInputControl(accountText);
	 	
		getFormToolkit().paintBordersFor(otherComposite);
		otherExpandableComposite.setClient(otherComposite);
	}


	private void createBookComposite() {
		bookSection = getFormToolkit().createSection(form.getBody(), Section.TITLE_BAR);
		bookSection.setText("Market data");
		bookSection.setExpanded(true);
			
		GridLayout gridLayout = new GridLayout();
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace=true;
		layoutData.grabExcessVerticalSpace=true;
		layoutData.verticalAlignment = SWT.FILL;
		layoutData.horizontalAlignment = SWT.FILL;
		layoutData.horizontalSpan = 5;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.numColumns = 2;

		bookSection.setLayout(gridLayout);
		bookSection.setLayoutData(layoutData);
		
		bookComposite = new BookComposite(bookSection, SWT.NONE, getFormToolkit());
		bookSection.setClient(bookComposite);
	}


	public void clear() {
		updateTitle(null);
		symbolText.setEnabled(true);
		sendButton.setEnabled(false);
	}

	public void updateMessage(Message aMessage) throws MarketceteraException
	{
		addCustomFields(aMessage);
	}
	
	public void clearMessage() {
		errorMessageLabel.setText("");
	}

	public void showMessage(Message order) {
		symbolText.setEnabled(FIXMessageUtil.isOrderSingle(order));
		updateTitle(order);
	}
	
	private void updateTitle(Message targetOrder)
	{
		if (targetOrder == null || !FIXMessageUtil.isCancelReplaceRequest(targetOrder)){
			form.setText(NEW_EQUITY_ORDER);
		} else {
			form.setText(REPLACE_EQUITY_ORDER);
		}
	}

	public static StockOrderTicket getDefault() {
		return (StockOrderTicket) PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().findView(
						StockOrderTicket.ID);
	}

	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (CustomOrderFieldPage.CUSTOM_FIELDS_PREFERENCE.equals(property)){
			String valueString = event.getNewValue().toString();
			updateCustomFields(valueString);
		}
	}

	private void updateCustomFields(String preferenceString) {
		// Save previous enabled checkbox state
		final int keyColumnNum = 1;
		HashMap<String, Boolean> existingEnabledMap = new HashMap<String, Boolean>(); 
		TableItem[] existingItems = customFieldsTable.getItems();
		for(TableItem existingItem : existingItems) {
			String key = existingItem.getText(keyColumnNum);
			boolean checkedState = existingItem.getChecked();
			existingEnabledMap.put(key, checkedState);
		}
		
		customFieldsTable.setItemCount(0);
		EventList<Entry<String, String>> fields = MapEditorUtil.parseString(preferenceString);
		for (Entry<String, String> entry : fields) {
			TableItem item = new TableItem(customFieldsTable, SWT.NONE);
			String key = entry.getKey();
			// Column order must match column numbers used above
			String[] itemText = new String[]{"", key, entry.getValue()};
			item.setText(itemText);
			if( existingEnabledMap.containsKey(key)) {
				boolean previousEnabledValue = existingEnabledMap.get(key);
				item.setChecked(previousEnabledValue);
			} 
		}
		TableColumn[] columns = customFieldsTable.getColumns();
		for (TableColumn column : columns) {
			column.pack();
		}
	}

	void addCustomFields(Message message) throws MarketceteraException {
		TableItem[] items = customFieldsTable.getItems();
		DataDictionary dictionary = FIXDataDictionaryManager.getCurrentFIXDataDictionary().getDictionary();
		for (TableItem item : items) {
			if (item.getChecked()) {
				String key = item.getText(1);
				String value = item.getText(2);
				int fieldNumber = -1;
				try {
					fieldNumber = Integer.parseInt(key);
				} catch (Exception e) {
					try {
						fieldNumber = dictionary.getFieldTag(key);
					} catch (Exception ex) {

					}
				}
				if (fieldNumber > 0) {
					if (dictionary.isHeaderField(fieldNumber)) {
						FIXMessageUtil.insertFieldIfMissing(fieldNumber, value, message
								.getHeader());
					} else if (dictionary.isTrailerField(fieldNumber)) {
						FIXMessageUtil.insertFieldIfMissing(fieldNumber, value, message
								.getTrailer());
					} else if (dictionary.isField(fieldNumber)) {
						FIXMessageUtil.insertFieldIfMissing(fieldNumber, value, message);
					}
				} else {
					throw new MarketceteraException("Could not find field "
							+ key);
				}
			}
		}
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		
		this.viewStateMemento = memento;
	}

	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		
		TableItem[] items = customFieldsTable.getItems();
		for (int i = 0; i < items.length; i++) {
			TableItem item = items[i];
			String key = CUSTOM_FIELD_VIEW_SAVED_STATE_KEY_PREFIX + item.getText(1);
			memento.putInteger(key, (item.getChecked() ? 1 : 0));
		}
	}


	/* (non-Javadoc)
	 * @see org.marketcetera.photon.views.IStockOrderTicket#getAccountText()
	 */
	public Text getAccountText() {
		return accountText;
	}

	/* (non-Javadoc)
	 * @see org.marketcetera.photon.views.IStockOrderTicket#getBookComposite()
	 */
	public BookComposite getBookComposite() {
		return bookComposite;
	}

	/* (non-Javadoc)
	 * @see org.marketcetera.photon.views.IStockOrderTicket#getCancelButton()
	 */
	public Button getCancelButton() {
		return cancelButton;
	}

	/* (non-Javadoc)
	 * @see org.marketcetera.photon.views.IStockOrderTicket#getCustomFieldsTable()
	 */
	public Table getCustomFieldsTable() {
		return customFieldsTable;
	}

	/* (non-Javadoc)
	 * @see org.marketcetera.photon.views.IStockOrderTicket#getErrorMessageLabel()
	 */
	public Label getErrorMessageLabel() {
		return errorMessageLabel;
	}

	/* (non-Javadoc)
	 * @see org.marketcetera.photon.views.IStockOrderTicket#getPriceText()
	 */
	public Text getPriceText() {
		return priceText;
	}

	/* (non-Javadoc)
	 * @see org.marketcetera.photon.views.IStockOrderTicket#getQuantityText()
	 */
	public Text getQuantityText() {
		return quantityText;
	}

	/* (non-Javadoc)
	 * @see org.marketcetera.photon.views.IStockOrderTicket#getSendButton()
	 */
	public Button getSendButton() {
		return sendButton;
	}

	/* (non-Javadoc)
	 * @see org.marketcetera.photon.views.IStockOrderTicket#getSideCCombo()
	 */
	public CCombo getSideCCombo() {
		return sideCCombo;
	}

	/* (non-Javadoc)
	 * @see org.marketcetera.photon.views.IStockOrderTicket#getSymbolText()
	 */
	public Text getSymbolText() {
		return symbolText;
	}

	/* (non-Javadoc)
	 * @see org.marketcetera.photon.views.IStockOrderTicket#getTableViewer()
	 */
	public CheckboxTableViewer getTableViewer() {
		return tableViewer;
	}

	/* (non-Javadoc)
	 * @see org.marketcetera.photon.views.IStockOrderTicket#getTifCCombo()
	 */
	public CCombo getTifCCombo() {
		return tifCCombo;
	}

	public void clearErrors() {
		showErrorMessage("", 0);
		for (Control aControl : inputControls) {
			Object cd;
			if (((cd = aControl.getData(CONTROL_DECORATOR_KEY)) != null)
					&& cd instanceof ControlDecoration) 
			{
				ControlDecoration controlDecoration = ((ControlDecoration)cd);
				controlDecoration.hide();
			}
			
		}
	}

	public void showErrorForControl(Control aControl, int severity, String message) {
		Object cd;
		if (((cd = aControl.getData(CONTROL_DECORATOR_KEY)) != null)
				&& cd instanceof ControlDecoration) 
		{
			ControlDecoration controlDecoration = ((ControlDecoration)cd);
			if (severity == IStatus.OK){
				controlDecoration.hide();
			} else {
				if (severity == IStatus.ERROR){
					controlDecoration.setImage(errorImage);
				} else {
					controlDecoration.setImage(warningImage);
				}
				if (message != null){
					controlDecoration.setDescriptionText(message);
				}
				controlDecoration.show();
			}
		}
	}

	public void showErrorMessage(String errorMessage, int severity) {
		if (errorMessage == null){
			errorMessageLabel.setText("");
		} else {
			errorMessageLabel.setText(errorMessage);
		}
		if (severity == IStatus.ERROR){
			sendButton.setEnabled(false);
		} else {
			sendButton.setEnabled(true);
		}
	}

}