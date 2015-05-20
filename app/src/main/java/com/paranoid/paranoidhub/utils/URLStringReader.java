package com.paranoid.paranoidhub.utils;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class URLStringReader extends AsyncTask<String, Void, Void> {

    private String mBuffer;
    private Exception mException;
    private URLStringReaderListener mListener;
    public URLStringReader(URLStringReaderListener listener) {
        mListener = listener;
    }

    @Override
    protected Void doInBackground(String... params) {
        mBuffer = null;
        try {
            mBuffer = readString(params[0]);
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (mListener != null) {
            if (mBuffer != null) {
                mListener.onReadEnd(mBuffer);
            } else if (mException != null) {
                mListener.onReadError(mException);
            }
        }
        super.onPostExecute(result);
    }

    private String readString(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        URLConnection yc = url.openConnection();
        BufferedReader in = null;
        StringBuffer sb = new StringBuffer();
        try {
            in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                sb.append(inputLine);
        } finally {
            if (in != null)
                in.close();
        }
        return sb.toString();
    }

    public interface URLStringReaderListener {

        void onReadEnd(String buffer);

        void onReadError(Exception ex);
    }
}