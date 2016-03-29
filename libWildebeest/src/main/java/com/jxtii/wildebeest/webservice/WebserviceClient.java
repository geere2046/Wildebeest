package com.jxtii.wildebeest.webservice;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.jxtii.wildebeest.bean.Page;
import com.jxtii.wildebeest.bean.PubData;
import com.jxtii.wildebeest.bean.PubDataList;
import com.jxtii.wildebeest.util.CommUtil;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by huangyc on 2016/3/18.
 */
public class WebserviceClient {

    static String TAG = WebserviceClient.class.getSimpleName();

    public PubData updateData(String reqxml) {
        Log.i(TAG, "reqxml=" + reqxml);

        PubData template = null;
        SoapObject request = new SoapObject(CommUtil.NAME_SPACE, "updateData");
        request.addProperty("reqxml", reqxml);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
        envelope.bodyOut = request;
        HttpTransportSE aht = null;
        try {
            aht = new HttpTransportSE(CommUtil.WS_URL, 60000);
            aht.call(null, envelope);
            if (envelope.getResponse() != null) {
                String xml = envelope.getResponse().toString();
                Log.i(TAG, "responseXml=" + xml);
                xml = xml.substring(1, xml.length() - 1);
                PubData planInfo = (PubData) JSON.parseObject(xml,
                        PubData.class);
                if ("00".equals(planInfo.getCode())
                        || "01".equals(planInfo.getCode())) {
                    template = planInfo;
                } else {
                    template = null;
                }
            }
        } catch (IOException e1) {
            template = null;
            e1.printStackTrace();
        } catch (XmlPullParserException e1) {
            template = null;
            e1.printStackTrace();
        } finally {
            aht = null;
        }
        return template;
    }

    public PubData loadData(String reqxml) {
        Log.i(TAG, "reqxml=" + reqxml);

        PubData template = null;
        SoapObject request = new SoapObject(CommUtil.NAME_SPACE, "loadData");
        request.addProperty("reqxml", reqxml);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
        envelope.bodyOut = request;
        HttpTransportSE aht = null;
        try {
            aht = new HttpTransportSE(CommUtil.WS_URL, 60000);
            aht.call(null, envelope);
            if (envelope.getResponse() != null) {
                String xml = envelope.getResponse().toString();
                Log.i(TAG, "responseXml=" + xml);
                xml = xml.substring(1, xml.length() - 1);
                PubData planInfo = (PubData) JSON.parseObject(xml,
                        PubData.class);
                if ("00".equals(planInfo.getCode())
                        || "01".equals(planInfo.getCode())) {
                    template = planInfo;
                } else {
                    template = null;
                }
            }
        } catch (IOException e1) {
            template = null;
            e1.printStackTrace();
        } catch (XmlPullParserException e1) {
            template = null;
            e1.printStackTrace();
        } finally {
            aht = null;
        }
        return template;
    }

    public PubDataList loadDataList(String reqxml) {
        Log.i(TAG, "reqxml=" + reqxml);

        PubDataList template = null;
        SoapObject request = new SoapObject(CommUtil.NAME_SPACE, "loadDataList");
        request.addProperty("reqxml", reqxml);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
        envelope.bodyOut = request;
        HttpTransportSE aht = null;
        try {
            aht = new HttpTransportSE(CommUtil.WS_URL, 60000);
            aht.call(null, envelope);
            if (envelope.getResponse() != null) {
                String xml = envelope.getResponse().toString();
                Log.i(TAG, "responseXml=" + xml);
                xml = xml.substring(1, xml.length() - 1);
                PubDataList planInfo = (PubDataList) JSON.parseObject(xml, PubDataList.class);
                if ("00".equals(planInfo.getCode())
                        || "01".equals(planInfo.getCode())) {

                    if (planInfo.getPage() == null) {
                        Page page = new Page();
                        page.setCurrentPage("1");
                        page.setPageCount("1");
                        planInfo.setPage(page);
                    }
                    template = planInfo;
                } else {
                    template = null;
                }
            }
        } catch (IOException e1) {
            template = null;
            e1.printStackTrace();
        } catch (XmlPullParserException e1) {
            template = null;
            e1.printStackTrace();
        } finally {
            aht = null;
        }
        return template;
    }
}
