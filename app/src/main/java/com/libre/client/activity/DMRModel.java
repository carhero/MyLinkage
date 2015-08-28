package com.libre.client.activity;

public class DMRModel {


		private int icon;
		private String title;
		

		private boolean isGroupHeader = false;

		public DMRModel(String title) {
			this(-1,title);
			isGroupHeader = true;
		}
		public DMRModel(int icon, String title) {
			super();
			this.icon = icon;
			this.title = title;
			
		}
		public int getIcon() {
			return icon;
		}
		public void setIcon(int icon) {
			this.icon = icon;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
	
		public boolean isGroupHeader() {
			return isGroupHeader;
		}
		public void setGroupHeader(boolean isGroupHeader) {
			this.isGroupHeader = isGroupHeader;
		}



	}


