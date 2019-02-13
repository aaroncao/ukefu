package com.ukefu.webim.web.handler;

import javax.servlet.http.HttpServletRequest;

import com.ukefu.webim.service.repository.OrgiSkillRelRepository;
import com.ukefu.webim.service.repository.TenantRepository;
import com.ukefu.webim.web.model.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ukefu.core.UKDataContext;
import com.ukefu.util.Menu;
import com.ukefu.webim.service.acd.ServiceQuene;
import com.ukefu.webim.service.cache.CacheHelper;
import com.ukefu.webim.service.repository.EkmKnowbaseConfigRepository;
import com.ukefu.webim.service.repository.EkmKnowbaseRepository;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ApplicationController extends Handler{
	
	@Autowired
	private EkmKnowbaseRepository ekmKnowbaseRes;
	
	@Autowired
	private EkmKnowbaseConfigRepository ekmKnowbaseConfigRes;

	@Autowired
	private OrgiSkillRelRepository orgiSkillRelRes;

	@Autowired
	private TenantRepository tenantRes;
	
	@RequestMapping("/")
	@Menu(type = "apps" , subtype = "root" , access=true)
    public ModelAndView root(HttpServletRequest request) {
		ModelAndView view = console(request);
		if(!StringUtils.isBlank(request.getServerName())) {
			EkmKnowbaseConfig kbconfig = ekmKnowbaseConfigRes.findByBasehost(request.getServerName()) ;
			if(kbconfig != null) {
				EkmKnowbase base = ekmKnowbaseRes.findOne(kbconfig.getKnowbaseid()) ;
				view = request(super.createRequestPageTempletResponse("redirect:/helpdesk/"+base.getKbviewid()+".html"));
			}
		}
		return view ;
	}
	
	
	@RequestMapping("/console")
    public ModelAndView console(HttpServletRequest request) {
		ModelAndView view = request(super.createRequestPageTempletResponse("/apps/index"));
		User user = super.getUser(request) ;
        view.addObject("agentStatusReport",ServiceQuene.getAgentReport(user.getOrgi())) ;
        view.addObject("tenant",super.getTenant(request));
        view.addObject("istenantshare",super.isEnabletneant());
        if(super.isEnabletneant()) {
			List<OrgiSkillRel> orgiSkillRelList = orgiSkillRelRes.findBySkillid((super.getUser(request)).getOrgan());
			List<Tenant> tenantList = null;
			if(!orgiSkillRelList.isEmpty()) {
				tenantList = new ArrayList<Tenant>();
				for(OrgiSkillRel orgiSkillRel:orgiSkillRelList) {
					Tenant t = tenantRes.findById(orgiSkillRel.getOrgi());
					if(t!=null) {
						tenantList.add(t);
					}
				}
			}
			if(tenantList!=null && tenantList.size()>0) {
				//多租户启用 非超级管理员 一定要选择租户才能进入界面
				if (!user.isSuperuser() && !StringUtils.isBlank(user.getOrgid()) && super.isTenantconsole() && UKDataContext.SYSTEM_ORGI.equals(user.getOrgi())) {
					view = request(super.createRequestPageTempletResponse("redirect:/apps/tenant/index"));
				}
				if (StringUtils.isBlank(user.getOrgid())) {
					view = request(super.createRequestPageTempletResponse("redirect:/apps/organization/add.html"));
				}
			}
        }
		view.addObject("agentStatus",CacheHelper.getAgentStatusCacheBean().getCacheObject(user.getId(), user.getOrgi())) ;
        return view;
    }
}