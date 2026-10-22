App({
  globalData: {
    // API 地址
    baseUrl: "http://114.215.181.113:8080",
    // 登录后的用户信息
    userInfo: null,
    // JWT Token
    token: "",
    loginPromise: null
  },

  onLaunch() {
    // 启动时自动 mock 登录
    this.autoLogin();
  },

  // ========== 自动登录 ==========
  autoLogin() {
    const token = wx.getStorageSync("token");
    if (token) {
      // 已有 token，校验是否有效
      this.globalData.token = token;
      wx.request({
        url: this.globalData.baseUrl + "/api/auth/check",
        method: "GET",
        header: { Authorization: "Bearer " + token },
        success: (res) => {
          if (res.data.code === 200) {
            this.globalData.userInfo = res.data.data.user;
            console.log("Token有效，已登录", this.globalData.userInfo);
          } else {
            // Token 无效，重新登录
            this.doMockLogin();
          }
        },
        fail: () => {
          console.warn("后端未启动，使用离线模式");
        }
      });
    } else {
      this.doMockLogin();
    }
  },

  // 切换身份
  switchRole(role) {
    var that = this;
    return new Promise(function (resolve) {
      wx.request({
        url: that.globalData.baseUrl + "/api/auth/login",
        method: "POST",
        data: { role: role },
        success: function (res) {
          if (res.data.code === 200) {
            var data = res.data.data;
            that.globalData.token = data.token;
            that.globalData.userInfo = data.user;
            wx.setStorageSync("token", data.token);
            resolve(data.user);
          }
        }
      });
    });
  },

  doMockLogin() {
    var that = this;
    that.globalData.loginPromise = new Promise(function (resolve) {
      wx.request({
        url: that.globalData.baseUrl + "/api/auth/login",
        method: "POST",
        data: { role: "user" },
        success: function (res) {
          if (res.data.code === 200) {
            var data = res.data.data;
            that.globalData.token = data.token;
            that.globalData.userInfo = data.user;
            wx.setStorageSync("token", data.token);
            console.log("Mock登录成功", data.user);
          }
          resolve();
        },
        fail: function () {
          console.warn("后端未启动，无法登录");
          resolve();
        }
      });
    });
  },

  // ========== 通用请求方法 ==========
  /**
   * @param {object} options - { url, method, data, auth, showLoading }
   * @returns {Promise}
   */
  request(options) {
    var that = this;
    var url = options.url, method = options.method || "GET", data = options.data || {}, auth = options.auth !== false, showLoading = options.showLoading;

    if (showLoading) {
      wx.showLoading({ title: "加载中...", mask: true });
    }

    // 如果还没登录成功，排队等着
    var doRequest = function () {
      var header = { "Content-Type": "application/json" };
      if (auth && that.globalData.token) {
        header["Authorization"] = "Bearer " + that.globalData.token;
      }

      return new Promise(function (resolve, reject) {
        wx.request({
          url: that.globalData.baseUrl + url,
          method: method,
          data: data,
          header: header,
          success: function (res) {
            if (showLoading) wx.hideLoading();
            if (res.data.code === 200) {
              resolve(res.data.data);
            } else if (res.data.code === 401 || res.data.code === 4002) {
              that.doMockLogin();
              reject(new Error(res.data.message || "登录过期"));
            } else {
              wx.showToast({ title: res.data.message || "请求失败", icon: "none" });
              reject(new Error(res.data.message || "请求失败"));
            }
          },
          fail: function (err) {
            if (showLoading) wx.hideLoading();
            wx.showToast({ title: "网络错误，请检查后端是否启动", icon: "none" });
            reject(err);
          }
        });
      });
    };

    // 如果有登录正在进行中，等它完成再发请求
    if (that.globalData.loginPromise) {
      return that.globalData.loginPromise.then(doRequest);
    }
    return doRequest();
  },

  // ========== 便捷方法 ==========
  get(url, data) {
    return this.request({ url, method: "GET", data });
  },
  post(url, data) {
    return this.request({ url, method: "POST", data });
  },
  put(url, data) {
    return this.request({ url, method: "PUT", data });
  },
  del(url, data) {
    return this.request({ url, method: "DELETE", data });
  },

  // ========== 上传图片 ==========
  uploadImage(tempFilePath) {
    return new Promise((resolve, reject) => {
      wx.uploadFile({
        url: this.globalData.baseUrl + "/api/files/upload",
        filePath: tempFilePath,
        name: "file",
        header: {
          Authorization: "Bearer " + this.globalData.token
        },
        success: (res) => {
          try {
            const data = JSON.parse(res.data);
            if (data.code === 200 && data.data) {
              resolve(data.data.url);
            } else {
              reject(new Error(data.message || "上传失败"));
            }
          } catch (e) {
            reject(new Error("解析上传结果失败"));
          }
        },
        fail: reject
      });
    });
  }
});
