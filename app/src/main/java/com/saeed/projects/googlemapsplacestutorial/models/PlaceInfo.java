package com.saeed.projects.googlemapsplacestutorial.models;

public class PlaceInfo
{

    private String placeId;
    private String paceName;
    private String placeAddress;
    private String placeCountry;
    private String placePhoneNo;
    private String placeWebSite;
    private String placeViewPort;
    private String placeAttributions;

    public String getPlaceId()
    {
        return placeId;
    }

    public void setPlaceId(String placeId)
    {
        this.placeId = placeId;
    }

    public String getPlaceName()
    {
        return paceName;
    }

    public void setPlaceName(String paceName)
    {
        this.paceName = paceName;
    }

    public String getPlaceAddress()
    {
        return placeAddress;
    }

    public void setPlaceAddress(String placeAddress)
    {
        this.placeAddress = placeAddress;
    }

    public String getPlaceCountry()
    {
        return placeCountry;
    }

    public void setPlaceCountry(String placeCountry)
    {
        this.placeCountry = placeCountry;
    }

    public String getPlacePhoneNo()
    {
        return placePhoneNo;
    }

    public void setPlacePhoneNo(String placePhoneNo)
    {
        this.placePhoneNo = placePhoneNo;
    }

    public String getPlaceWebSite()
    {
        if(placeAddress == null)
            return "";
        return placeWebSite;
    }

    public void setPlaceWebSite(String placeWebSite)
    {
        this.placeWebSite = placeWebSite;
    }

    public String getPlaceViewPort()
    {
        return placeViewPort;
    }

    public void setPlaceViewPort(String placeViewPort)
    {
        this.placeViewPort = placeViewPort;
    }

    public String getPlaceAttributions()
    {
        return placeAttributions;
    }

    public void setPlaceAttributions(String placeAttributions)
    {
        this.placeAttributions = placeAttributions;
    }
}
