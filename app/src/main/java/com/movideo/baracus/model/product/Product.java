package com.movideo.baracus.model.product;

import com.google.gson.annotations.SerializedName;
import com.movideo.baracus.model.common.Image;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Product implements Serializable, Comparable<Product>
{

	private static final long serialVersionUID = 1L;
	private Integer id;
	private String object;
	private String type;
	private String status;
	private String title;
	private String season;
	private String episode;
	private String description;
	private String duration;
	private String released;
	private Language language;
	private List<String> countries = new ArrayList<String>();
	private String distributor;
	private String licensee;
	private String classification;
	private List<String> genres = new ArrayList<>();
	private List<String> actors = new ArrayList<String>();
	private List<String> writers = new ArrayList<String>();
	private List<String> directors = new ArrayList<>();
	@SerializedName("package_type")
	private String packageType;
	@SerializedName("created_at")
	private Date createdDate;
	@SerializedName("modified_at")
	private Date modifiedDate;
	private String copyright;
	private Image image;
	@SerializedName("has_parent")
	private Boolean hasParent;
	@SerializedName("has_children")
	private Boolean hasChildren;
	private List<Offerings> offerings = new ArrayList<Offerings>();
	private List<Promotions> promotions = new ArrayList<Promotions>();
	private String format;
	private String date;
	private String amount;
	private String currency;
	private Product parentProduct;
	private String trailer;
	private String poster;
	private String background;
	private Integer category;
	public Date getCreatedDate()
	{
		return createdDate;
	}

	public void setCreatedDate(Date createdDate)
	{
		this.createdDate = createdDate;
	}

	public Date getModifiedDate()
	{
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate)
	{
		this.modifiedDate = modifiedDate;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getSeason()
	{
		return season;
	}

	public void setSeason(String season)
	{
		this.season = season;
	}

	public String getEpisode()
	{
		return episode;
	}

	public void setEpisode(String episode)
	{
		this.episode = episode;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getDuration()
	{
		return duration;
	}

	public void setDuration(String duration)
	{
		this.duration = duration;
	}

	public String getReleased()
	{
		return released;
	}

	public void setReleased(String released)
	{
		this.released = released;
	}

	public Language getLanguage()
	{
		return language;
	}

	public void setLanguage(Language language)
	{
		this.language = language;
	}

	public List<String> getCountries()
	{
		return countries;
	}

	public void setCountries(List<String> countries)
	{
		this.countries = countries;
	}

	public String getDistributor()
	{
		return distributor;
	}

	public void setDistributor(String distributor)
	{
		this.distributor = distributor;
	}

	public String getLicensee()
	{
		return licensee;
	}

	public void setLicensee(String licensee)
	{
		this.licensee = licensee;
	}

	public String getClassification()
	{
		return classification;
	}

	public void setClassification(String classification)
	{
		this.classification = classification;
	}

	public List<String> getGenres()
	{
		return genres;
	}

	public void setGenres(List<String> genres)
	{
		this.genres=genres;
	}

	public List<String> getActors()
	{
		return actors;
	}

	public void setActors(List<String> actors)
	{
		this.actors = actors;
	}

	public List<String> getWriters()
	{
		return writers;
	}

	public void setWriters(List<String> writers)
	{
		this.writers = writers;
	}

	public List<String> getDirectors()
	{
		return directors;
	}

	public void setDirectors(List<String> directors)
	{
		this.directors = directors;
	}

	public String getCopyright()
	{
		return copyright;
	}

	public void setCopyright(String copyright)
	{
		this.copyright = copyright;
	}

	public Integer getId()
	{
		return this.id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public String getObject()
	{
		return this.object;
	}

	public void setObject(String object)
	{
		this.object = object;
	}

	public String getType()
	{
		return this.type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getFormat()
	{
		return this.format;
	}

	public void setFormat(String format)
	{
		this.format = format;
	}

	public String getStatus()
	{
		return this.status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public Boolean getHasParent()
	{
		return this.hasParent;
	}

	public void setHasParent(Boolean hasParent)
	{
		this.hasParent = hasParent;
	}

	public Boolean getHasChildren()
	{
		return this.hasChildren;
	}

	public void setHasChildren(Boolean hasChildren)
	{
		this.hasChildren = hasChildren;
	}

	public String getDate()
	{
		return date;
	}

	public void setDate(String date)
	{
		this.date = date;
	}

	public String getAmount()
	{
		return amount;
	}

	public void setAmount(String amount)
	{
		this.amount = amount;
	}

	public String getCurrency()
	{
		return currency;
	}

	public void setCurrency(String currency)
	{
		this.currency = currency;
	}

	public String getBackground() {
		return background;
	}

	public void setBackground(String background) {
		this.background = background;
	}

	public String getPoster() {
		return poster;
	}

	public void setPoster(String poster) {
		this.poster = poster;
	}

	public Image getImage()
	{
		return this.image;
	}

	public void setImage(Image image)
	{
		this.image = image;
	}

	public List<Offerings> getOfferings()
	{
		return this.offerings;
	}

	public void setOfferings(List<Offerings> offerings)
	{
		this.offerings = offerings;
	}

	public List<Promotions> getPromotions()
	{
		return this.promotions;
	}

	public void setPromotions(List<Promotions> promotions)
	{
		this.promotions = promotions;
	}

	public Integer getCategory() {
		return category;
	}

	public void setCategory(Integer category) {
		this.category = category;
	}

	public String getPackageType() {
		return packageType;
	}

	public void setPackageType(String packageType) {
		this.packageType = packageType;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Prod[")
				.append("id : ").append(id).append(",\n")
				.append("object : ").append(object).append(",\n")
				.append("type : ").append(type).append(",\n")
				.append("status : ").append(status).append(",\n")
				.append("title : ").append(title).append(",\n")
				.append("season : ").append(season).append(",\n")
				.append("episode : ").append(episode).append(",\n")
				.append("description : ").append(description).append(",\n")
				.append("duration : ").append(duration).append(",\n")
				.append("released : ").append(released).append(",\n")
				.append("language : ").append(language).append(",\n")
				.append("countries : ").append(countries).append(",\n")
				.append("distributor : ").append(distributor).append(",\n")
				.append("licensee : ").append(licensee).append(",\n")
				.append("classification : ").append(classification).append(",\n")
				.append("genres : ").append(genres).append(",\n")
				.append("actors : ").append(actors).append(",\n")
				.append("writers : ").append(writers).append(",\n")
				.append("directors : ").append(directors).append(",\n")
				.append("createdAt : ").append(createdDate).append(",\n")
				.append("modifiedAt : ").append(modifiedDate).append(",\n")
				.append("copyright : ").append(copyright).append(",\n")
				//.append("image : ").append(image).append(",\n")
				.append("hasParent : ").append(hasParent).append(",\n")
				.append("hasChildren : ").append(hasChildren).append(",\n")
				.append("offerings : ").append(offerings).append(",\n")
				.append("promotions : ").append(promotions).append(",\n")
				.append("format : ").append(format).append(",\n")
				.append("amount : ").append(amount).append(",\n")
				.append("currency : ").append(currency).append(",\n")
				.append("date : ").append(date).append(",\n")
				.append("package_type : ").append(packageType).append(",\n")
				.append("trailer : ").append(trailer).append(",\n").append("]");
		return builder.toString();
	}

	public void setParentProduct(Product parentProduct) {
		this.parentProduct = parentProduct;
	}

	public Product getParentProduct() {
		return parentProduct;
	}

	public String getTrailer() {
		return trailer;
	}

	public void setTrailer(String trailer) {
		this.trailer = trailer;
	}

	@Override
	public int compareTo(Product another) {
		return this.getId().compareTo(another.getId());
	}
}
