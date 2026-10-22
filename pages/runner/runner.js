const app = getApp();

var TYPE_ICONS = {
  "快递": "快",
  "外卖": "餐",
  "文件": "文",
  "钥匙": "钥",
  "代购": "购",
  "其他": "其"
};

var FILTERS = ["全部", "待接单", "进行中", "已完成"];

function getStatusKey(status) {
  var map = {
    "待接单": "pending",
    "已接单": "accepted",
    "已取件": "picked",
    "配送中": "delivering",
    "待确认": "confirming",
    "已完成": "done",
    "已取消": "cancelled"
  };
  return map[status] || "done";
}

function getUrgentKey(urgent) {
  return urgent === "加急" || urgent === "紧急" || urgent === "极速" ? "urgent" : "normal";
}

function normalizeOrder(item) {
  var type = item.type || "订单";
  var urgency = item.urgency || item.urgent || "普通";
  item.type = type;
  item.typeIcon = TYPE_ICONS[type] || "其";
  item.title = item.title || (type + "取件");
  item.from = item.from || item.pickupLocation;
  item.to = item.to || item.deliveryLocation;
  item.urgency = urgency;
  item.urgent = urgency;
  item.statusKey = item.statusKey || getStatusKey(item.status);
  item.urgentKey = item.urgentKey || getUrgentKey(urgency);
  return item;
}

Page({
  data: {
    orders: [],
    displayOrders: [],
    filters: FILTERS,
    activeFilter: "全部",
    stats: { available: 0, today: 0, earned: 0 }
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 1 });
    }
    this.refresh();
  },

  refresh() {
    var that = this;
    app.get("/api/stats/runner").then(function (stats) {
      that.setData({ stats: stats || { available: 0, today: 0, earned: 0 } });
    }).catch(function (err) {
      console.error("加载统计失败", err);
    });

    app.get("/api/orders", { page: 1, size: 100 }).then(function (data) {
      var orders = (data.records || []).map(normalizeOrder);
      that.setData({ orders: orders });
      that.applyFilter();
    }).catch(function (err) {
      console.error("加载跑腿数据失败", err);
    });
  },

  setFilter(e) {
    this.setData({ activeFilter: e.currentTarget.dataset.value });
    this.applyFilter();
  },

  applyFilter() {
    var orders = this.data.orders;
    var activeFilter = this.data.activeFilter;
    var displayOrders;
    if (activeFilter === "全部") {
      displayOrders = orders;
    } else if (activeFilter === "进行中") {
      displayOrders = orders.filter(function (o) {
        return ["已接单", "已取件", "配送中", "待确认"].indexOf(o.status) > -1;
      });
    } else {
      displayOrders = orders.filter(function (o) { return o.status === activeFilter; });
    }
    this.setData({ displayOrders: displayOrders });
  },

  accept(event) {
    var id = event.currentTarget.dataset.id;
    var that = this;
    wx.showModal({
      title: "确认接单",
      content: "接单后请在规定时间内完成配送。",
      success: function (res) {
        if (res.confirm) {
          app.post("/api/orders/" + id + "/accept").then(function () {
            wx.showToast({ title: "接单成功", icon: "success" });
            that.refresh();
          }).catch(function (err) {
            console.error("接单失败", err);
          });
        }
      }
    });
  },

  startDelivery(event) {
    var id = event.currentTarget.dataset.id;
    var that = this;
    wx.showModal({
      title: "开始配送",
      content: "确认已取到物品，开始配送？",
      success: function (res) {
        if (res.confirm) {
          app.post("/api/orders/" + id + "/deliver").then(function () {
            wx.showToast({ title: "已开始配送", icon: "success" });
            that.refresh();
          }).catch(function (err) {
            console.error("操作失败", err);
          });
        }
      }
    });
  },

  uploadPickup(event) {
    var id = event.currentTarget.dataset.id;
    var that = this;
    wx.chooseMedia({
      count: 3,
      mediaType: ["image"],
      sourceType: ["album", "camera"],
      success: function (res) {
        wx.showLoading({ title: "上传中..." });
        that.uploadImages(res.tempFiles, 0, [], function (urls) {
          app.post("/api/orders/" + id + "/pickup", { images: urls }).then(function () {
            wx.hideLoading();
            wx.showToast({ title: "取件凭证已上传", icon: "success" });
            that.refresh();
          }).catch(function (err) {
            wx.hideLoading();
            console.error("上传失败", err);
          });
        });
      }
    });
  },

  uploadDelivery(event) {
    var id = event.currentTarget.dataset.id;
    var that = this;
    wx.chooseMedia({
      count: 3,
      mediaType: ["image"],
      sourceType: ["album", "camera"],
      success: function (res) {
        wx.showLoading({ title: "上传中..." });
        that.uploadImages(res.tempFiles, 0, [], function (urls) {
          app.post("/api/orders/" + id + "/complete", { images: urls }).then(function () {
            wx.hideLoading();
            wx.showToast({ title: "已上传送达凭证，等待下单人确认", icon: "success" });
            that.refresh();
          }).catch(function (err) {
            wx.hideLoading();
            console.error("上传失败", err);
          });
        });
      }
    });
  },

  uploadImages(files, index, urls, callback) {
    var that = this;
    if (index >= files.length) {
      callback(urls);
      return;
    }
    app.uploadImage(files[index].tempFilePath).then(function (url) {
      urls.push(url);
      that.uploadImages(files, index + 1, urls, callback);
    }).catch(function (err) {
      wx.hideLoading();
      wx.showToast({ title: "图片上传失败", icon: "none" });
      console.error(err);
    });
  },

  goDetail(event) {
    wx.navigateTo({ url: "/pages/detail/detail?id=" + event.currentTarget.dataset.id });
  }
});
