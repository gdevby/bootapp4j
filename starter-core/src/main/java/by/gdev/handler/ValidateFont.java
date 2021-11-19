package by.gdev.handler;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class ValidateFont extends ValisatedEnviromentAbstract {

	@Override
	public boolean validate() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			return true;
	}

	@Override
	public String getExceptionMessage() {
		return localizationBandle.getString("validate.font");
	}
}