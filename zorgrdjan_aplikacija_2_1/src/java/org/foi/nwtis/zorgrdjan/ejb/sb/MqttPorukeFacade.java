/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.zorgrdjan.ejb.sb;

import org.foi.nwtis.zorgrdjan.ejb.eb.MqttPoruke;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Zoran
 */
@Stateless
public class MqttPorukeFacade extends AbstractFacade<MqttPoruke> {

    @PersistenceContext(unitName = "zorgrdjan_aplikacija_2_1PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public MqttPorukeFacade() {
        super(MqttPoruke.class);
    }
    
}
