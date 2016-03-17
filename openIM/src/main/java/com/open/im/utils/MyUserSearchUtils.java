package com.open.im.utils;

import java.util.List;

import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.xdata.Form;

import com.open.im.app.MyApp;

public class MyUserSearchUtils {

	/**
	 * 方法 判断用户是否存在 注意 用户必须设置的个人信息才能查的到 如果使用两个参数的注册方法 注册后是不能直接查的到的
	 * 必须用户设置个人信息后才能查的 推荐使用三个参数的方法进行注册
	 * 
	 * @param username
	 * @return
	 */
	public static boolean isUserExist(String username) {
		boolean isExist = true;
		String userJid = MyApp.username + "@" + MyApp.connection.getServiceName();
		List<Row> list = searchUsers(username);
		if (list == null || list.size() == 0) {
			isExist = false;
		} else {
			String friendJid = list.get(0).getValues("jid").get(0);
			if (userJid.equals(friendJid)){
				isExist = false;
			}
		}
		return isExist;
	}

	/**
	 * 方法 根据用户名 查找用户
	 * 
	 * @param username
	 * @return
	 */
	public static List<Row> searchUsers(String username) {
		List<Row> list = null;
		// 获取用户搜查者对象
		try {
			UserSearchManager usm = new UserSearchManager(MyApp.connection);
			String serviceName = MyApp.connection.getServiceName();
			// 获得要查询的表格 参数为vjud. + 服务名称
			Form searchForm = usm.getSearchForm("vjud." + serviceName);
//			 Form searchForm = usm.getSearchForm(serviceName);
			// 根据要查询的表创建一个新的表格 填充查询出来的数据
			Form answerForm = searchForm.createAnswerForm();
			// 设置要查询的字段 和 查询的依据
			answerForm.setAnswer("user", username);
			// 查询
			ReportedData data = usm.getSearchResults(answerForm, "vjud." + serviceName);
			// 获取查出来的行
			list = data.getRows();

		} catch (NoResponseException e) {
			e.printStackTrace();
		} catch (XMPPErrorException e) {
			e.printStackTrace();
		} catch (NotConnectedException e) {
			e.printStackTrace();
		}

		return list;
	}
}
