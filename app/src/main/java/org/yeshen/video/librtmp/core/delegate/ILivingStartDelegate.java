package org.yeshen.video.librtmp.core.delegate;

import android.support.annotation.WorkerThread;

/*********************************************************************
 * Created by yeshen on 2017/05/26.
 * Copyright (c) 2017 yeshen.org. - All Rights Reserved
 *********************************************************************/


public interface ILivingStartDelegate {
    @WorkerThread
    void error(int error);

    @WorkerThread
    void success();
}
