package tests;

import org.openqa.selenium.By;

import base.BasePageSelenium;

public class PageWebGoogle extends BasePageSelenium {
	
	private static By inputSearch = By.xpath("//input[@name='q']");
	
	
	public static void search(String text) throws Exception {
		BasePageSelenium.waitSendTextPressEnter(inputSearch, text);

	}
	

}
