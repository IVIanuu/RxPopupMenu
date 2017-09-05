/*
 * Copyright 2017 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivianuu.rxpopupmenu;

import android.support.annotation.CheckResult;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.disposables.Disposable;

/**
 * Rx popup menu
 */
public final class RxPopupMenu implements MaybeOnSubscribe<MenuItem> {

    private final PopupMenu popupMenu;

    private RxPopupMenu(PopupMenu popupMenu) {
        this.popupMenu = popupMenu;
    }

    /**
     * Emits on popup clicks
     */
    @CheckResult @NonNull
    public static Maybe<MenuItem> create(@NonNull View anchor,
                                         @MenuRes int menuRes) {
        return create(anchor, menuRes, Gravity.NO_GRAVITY);
    }

    /**
     * Emits on popup clicks
     */
    @CheckResult @NonNull
    public static Maybe<MenuItem> create(@NonNull View anchor,
                                         @MenuRes int menuRes,
                                         int gravity) {
        PopupMenu popupMenu = new PopupMenu(anchor.getContext(), anchor);
        popupMenu.inflate(menuRes);
        popupMenu.setGravity(gravity);

        return create(popupMenu);
    }

    /**
     * Emits on popup clicks
     */
    @CheckResult @NonNull
    public static Maybe<MenuItem> create(@NonNull PopupMenu popupMenu) {
        return Maybe.create(new RxPopupMenu(popupMenu));
    }

    @Override
    public void subscribe(final MaybeEmitter<MenuItem> e) throws Exception {
        // success on clicks
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (!e.isDisposed()) {
                    e.onSuccess(item);
                    e.onComplete();
                }
                return true;
            }
        });

        // complete on dismiss
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                if (!e.isDisposed()) {
                    e.onComplete();
                }
            }
        });

        // dismiss pop up on dispose
        e.setDisposable(new Disposable() {
            private boolean disposed;
            @Override
            public void dispose() {
                if (!disposed) {
                    disposed = true;
                    popupMenu.dismiss();
                }
            }

            @Override
            public boolean isDisposed() {
                return disposed;
            }
        });

        // show
        popupMenu.show();
    }
}