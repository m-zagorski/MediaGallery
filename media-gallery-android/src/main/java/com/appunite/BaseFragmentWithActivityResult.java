package com.appunite;


import android.content.Intent;

public abstract class BaseFragmentWithActivityResult extends BaseFragment {

    public abstract void onActivityResultFix(int requestCode, int resultCode, Intent data);
}
