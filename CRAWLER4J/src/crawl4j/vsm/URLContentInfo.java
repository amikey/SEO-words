package crawl4j.vsm;

public class URLContentInfo {
	private String attributes;
	private int nb_attributes;
	private String content;
	private String url;
	private String magasin;
	private String produit;
	private String rayon;
	private String pageType;
	private boolean isCdiscountVendor;
	
	public String getAttributes() {
		return attributes;
	}
	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}
	public int getNb_attributes() {
		return nb_attributes;
	}
	public void setNb_attributes(int nb_attributes) {
		this.nb_attributes = nb_attributes;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getMagasin() {
		return magasin;
	}
	public void setMagasin(String magasin) {
		this.magasin = magasin;
	}
	public String getProduit() {
		return produit;
	}
	public void setProduit(String produit) {
		this.produit = produit;
	}
	public String getRayon() {
		return rayon;
	}
	public void setRayon(String rayon) {
		this.rayon = rayon;
	}
	public String getPageType() {
		return pageType;
	}
	public void setPageType(String pageType) {
		this.pageType = pageType;
	}
	public boolean isCdiscountVendor() {
		return isCdiscountVendor;
	}
	public void setCdiscountVendor(boolean isCdiscountVendor) {
		this.isCdiscountVendor = isCdiscountVendor;
	}
}
