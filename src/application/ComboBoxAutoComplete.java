package application;

import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;

/**
 * 
 * Uses a combobox tooltip as the suggestion for auto complete and updates the
 * combo box itens accordingly.
 * @author wsiqueir
 *
 * @param <T>
 */
public class ComboBoxAutoComplete<T> {

	private ComboBox<T> cmb;
	String filter = "";
	String topSearch = "";
	boolean useTopSearch = false;
	private ObservableList<T> originalItems;

	public ComboBoxAutoComplete(ComboBox<T> cmb) {
		this.cmb = cmb;
		originalItems = FXCollections.observableArrayList(cmb.getItems());
		cmb.setTooltip(new Tooltip());
		cmb.setOnKeyPressed(this::handleOnKeyPressed);
		cmb.setOnHidden(this::handleOnHiding);
	}

	public void handleOnKeyPressed(KeyEvent e) {
	
		ObservableList<T> filteredList = FXCollections.observableArrayList();
		KeyCode code = e.getCode();

		if (code.isLetterKey()) {
			filter += e.getText();
		}
		if (code == KeyCode.BACK_SPACE && filter.length() > 0) {
			filter = filter.substring(0, filter.length() - 1);
			cmb.getItems().setAll(originalItems);
		}
		if (code == KeyCode.ESCAPE) {
			filter = "";
		}
		if (code == KeyCode.TAB || code == KeyCode.ENTER) { 
			
			useTopSearch = true;
		}
		if (filter.length() == 0) {
			filteredList = originalItems;
			cmb.getTooltip().hide();
		} else {
			Stream<T> itens = cmb.getItems().stream();
			String txtUsr = filter.toString().toLowerCase();
			itens.filter(el -> el.toString().toLowerCase().replace("'", "").contains(txtUsr)).forEach(filteredList::add);
			cmb.getTooltip().setText(txtUsr);

			if(filteredList.size() > 0){
				topSearch = filteredList.get(0).toString();
			}
			Window stage = cmb.getScene().getWindow();
			double posX = stage.getX() + cmb.localToScene(cmb.getBoundsInLocal()).getMinX();
			double posY = stage.getY() + cmb.localToScene(cmb.getBoundsInLocal()).getMinY();
			cmb.getTooltip().show(stage, posX, posY);
			cmb.show();
		}
		cmb.getItems().setAll(filteredList);

	}

	/** Handles when the ComboBox is closed. */
	@SuppressWarnings("unchecked")
	public void handleOnHiding(Event e) {
		filter = "";
		cmb.getTooltip().hide();
		T s = null;
		s = (T) topSearch;

		if(useTopSearch && cmb.getSelectionModel().isEmpty() && cmb.getValue() == null && cmb.getSelectionModel().getSelectedItem() == null){ // Top of search results
			System.out.println("using top search");
			s = (T) topSearch;
			cmb.getSelectionModel().select(s);

		}else if(useTopSearch){ // Selected Item
			System.out.println("using getselecteditem");
			int index = cmb.getSelectionModel().getSelectedIndex();
			cmb.getSelectionModel().select(s);
			cmb.getSelectionModel().select(cmb.getSelectionModel().getSelectedIndex() - index);
		}else{
			s = cmb.getSelectionModel().getSelectedItem();
			cmb.getSelectionModel().select(s);
		}
		cmb.getItems().setAll(originalItems);
		useTopSearch = false;
		//cmb.getSelectionModel().select(s);

	}
}