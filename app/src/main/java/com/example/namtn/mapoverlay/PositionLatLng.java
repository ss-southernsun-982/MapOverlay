package com.example.namtn.mapoverlay;

class PositionLatLng {

    private String Post_lat;
    private String Post_long;
    private String Source_id;

    public PositionLatLng(String post_lat, String post_long, String source_id) {
        Post_lat = post_lat;
        Post_long = post_long;
        Source_id = source_id;
    }

    public String getPost_lat() {
        return Post_lat;
    }

    public void setPost_lat(String post_lat) {
        Post_lat = post_lat;
    }

    public String getPost_long() {
        return Post_long;
    }

    public void setPost_long(String post_long) {
        Post_long = post_long;
    }

    public String getSource_id() {
        return Source_id;
    }

    public void setSource_id(String source_id) {
        Source_id = source_id;
    }
}
