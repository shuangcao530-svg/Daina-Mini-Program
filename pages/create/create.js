const app = getApp();

Page({
  data: {
    types: ["快递", "外卖", "文件", "钥匙", "其他"],
    typeIndex: 0,
    urgents: [
      { name: "普通", selected: "active" },
      { name: "加急", selected: "" },
      { name: "极速", selected: "" }
    ],
    urgent: "普通",
    from: "",
    to: "",
    pickupCode: "",
    note: "",
    fee: "",
    images: [],
    uploading: false
  },
  onLoad(options) {
    if (options.type) {
      var typeIndex = this.data.types.indexOf(options.type);
      if (typeIndex > -1) {
        this.setData({ typeIndex: typeIndex });
      }
    }
  },
  onTypeChange(event) {
    this.setData({ typeIndex: Number(event.detail.value) });
  },
  setUrgent(event) {
    var value = event.currentTarget.dataset.value;
    var urgents = this.data.urgents.map(function (item) {
      return { name: item.name, selected: item.name === value ? "active" : "" };
    });
    this.setData({ urgent: value, urgents: urgents });
  },
  onInput(event) {
    this.setData({ [event.currentTarget.dataset.field]: event.detail.value });
  },
  onFeeInput(event) {
    this.setData({ fee: event.detail.value });
  },
  chooseImage() {
    var that = this;
    wx.chooseMedia({
      count: 3 - that.data.images.length,
      mediaType: ["image"],
      sourceType: ["album", "camera"],
      success: function (res) {
        var picked = res.tempFiles.map(function (item) { return item.tempFilePath; });
        that.setData({ images: that.data.images.concat(picked).slice(0, 3) });
      }
    });
  },

  submitOrder() {
    var that = this;
    if (!this.data.from || !this.data.to) {
      wx.showToast({ title: "请填写取件和送达地点", icon: "none" });
      return;
    }
    if (!this.data.images.length) {
      wx.showToast({ title: "请先上传物品照片", icon: "none" });
      return;
    }
    var feeNum = parseFloat(this.data.fee);
    if (!this.data.fee || isNaN(feeNum) || feeNum <= 0) {
      wx.showToast({ title: "请输入合理的配送费", icon: "none" });
      return;
    }

    // 先上传图片到后端
    wx.showLoading({ title: "上传图片中..." });
    that.uploadImages(0, [], function (imageUrls) {
      wx.hideLoading();
      // 创建订单
      app.post("/api/orders", {
        type: that.data.types[that.data.typeIndex],
        pickupLocation: that.data.from,
        deliveryLocation: that.data.to,
        pickupCode: that.data.pickupCode,
        urgency: that.data.urgent,
        fee: feeNum,
        note: that.data.note,
        contactName: "当前用户",
        contactPhone: "",
        images: imageUrls
      }).then(function () {
        wx.showToast({ title: "发布成功" });
        setTimeout(function () {
          wx.navigateTo({ url: "/pages/orders/orders" });
        }, 600);
      }).catch(function (err) {
        wx.showToast({ title: "发布失败", icon: "none" });
        console.error(err);
      });
    });
  },

  // 递归上传图片
  uploadImages(index, urls, callback) {
    var that = this;
    if (index >= that.data.images.length) {
      callback(urls);
      return;
    }
    app.uploadImage(that.data.images[index]).then(function (url) {
      urls.push(url);
      that.uploadImages(index + 1, urls, callback);
    }).catch(function (err) {
      wx.hideLoading();
      wx.showToast({ title: "图片上传失败", icon: "none" });
      console.error(err);
    });
  }
});
