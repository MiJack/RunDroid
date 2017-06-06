package cn.mijack.multithreadandipcdemo;

import android.os.AsyncTask;
import android.widget.Button;

/**
 * Created by Mr.Yuan on 2017/3/29.
 */

public class MyAsyncTask extends AsyncTask<Void, Integer, Void> {

    private Button button;

    public MyAsyncTask(Button button) {
        this.button = button;
    }

    @Override
    protected void onPreExecute() {
        button.setEnabled(false);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        for (int i = 0; i < 20; i++) {
            publishProgress(i);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        button.setText(String.valueOf(values[values.length - 1]));
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        button.setText(R.string.start_asynctask);
        button.setEnabled(true);
        button = null;
    }
}
