Page({
  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 0 });
    }
  },

  data: {
    services: [
      { name: "快递", icon: "快", desc: "驿站代取" },
      { name: "外卖", icon: "餐", desc: "餐品送达" },
      { name: "文件", icon: "文", desc: "资料转送" },
      { name: "钥匙", icon: "钥", desc: "随手急送" }
    ],
    highlights: [
      { value: "12 分钟", label: "平均接单" },
      { value: "3 次", label: "照片凭证" },
      { value: "校内", label: "限定范围" }
    ],
    steps: [
      { title: "拍照发布", desc: "上传物品照片，填写取件点和送达点。" },
      { title: "接单取件", desc: "跑腿员确认接单，取件后上传凭证。" },
      { title: "送达完成", desc: "送达后补充照片，订单自动进入完成状态。" }
    ]
  },
  goCreate() {
    wx.navigateTo({ url: "/pages/create/create" });
  },
  quickCreate(event) {
    wx.navigateTo({ url: `/pages/create/create?type=${event.currentTarget.dataset.type}` });
  }
});
