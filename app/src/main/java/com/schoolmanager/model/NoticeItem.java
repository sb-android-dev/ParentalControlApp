package com.schoolmanager.model;

public class NoticeItem {

    private int noticeId;
    private String noticeName;
    private String noticeDetail;
    private long noticeTime;
    private String noticeThumbImage;
    private String noticeMainImage;

    public NoticeItem() {
    }

    public NoticeItem(int noticeId, String noticeName, String noticeDetail, long noticeTime,
                      String noticeThumbImage, String noticeMainImage) {
        this.noticeId = noticeId;
        this.noticeName = noticeName;
        this.noticeDetail = noticeDetail;
        this.noticeTime = noticeTime;
        this.noticeThumbImage = noticeThumbImage;
        this.noticeMainImage = noticeMainImage;
    }

    public int getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(int noticeId) {
        this.noticeId = noticeId;
    }

    public String getNoticeName() {
        return noticeName;
    }

    public void setNoticeName(String noticeName) {
        this.noticeName = noticeName;
    }

    public String getNoticeDetail() {
        return noticeDetail;
    }

    public void setNoticeDetail(String noticeDetail) {
        this.noticeDetail = noticeDetail;
    }

    public long getNoticeTime() {
        return noticeTime;
    }

    public void setNoticeTime(long noticeTime) {
        this.noticeTime = noticeTime;
    }

    public String getNoticeThumbImage() {
        return noticeThumbImage;
    }

    public void setNoticeThumbImage(String noticeThumbImage) {
        this.noticeThumbImage = noticeThumbImage;
    }

    public String getNoticeMainImage() {
        return noticeMainImage;
    }

    public void setNoticeMainImage(String noticeMainImage) {
        this.noticeMainImage = noticeMainImage;
    }
}
