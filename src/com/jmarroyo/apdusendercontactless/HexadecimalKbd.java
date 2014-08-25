/*
Copyright 2014  Jose Maria ARROYO jm.arroyo.castejon@gmail.com

APDUSenderContactLess is free software: you can redistribute it and/or modify
it  under  the  terms  of the GNU General Public License  as published by the 
Free Software Foundation, either version 3 of the License, or (at your option) 
any later version.

APDUSenderContactLess is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

package com.jmarroyo.apdusendercontactless;

import android.app.Activity;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.text.Editable;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;


class HexadecimalKbd 
{
    private boolean bFirstIteration=false;
    private KeyboardView mKeyboardView;
    private Activity mHostActivity;

    private OnKeyboardActionListener mOnKeyboardActionListener = new OnKeyboardActionListener()
    {
        public final static int CodeDelete   = -5; //DELETE
        public final static int CodeLeft     = 55002;
        public final static int CodeRight    = 55003;
        public final static int CodeClear    = 55006;

        @Override public void onKey(int primaryCode, int[] keyCodes)
        {
            View focusCurrent = mHostActivity.getWindow().getCurrentFocus();
            if( focusCurrent==null || (focusCurrent.getClass()!=EditText.class) )
            {
                return;
            }
            EditText edittext = (EditText) focusCurrent;
            Editable editable = edittext.getText();
            int start = edittext.getSelectionStart();
            if( primaryCode==CodeDelete ) 
            {
                if( (editable!=null) && (start>0) ) 
                {
                    editable.delete(start - 1, start);
                }
            } 
            else if( primaryCode==CodeClear )
            {
                if( editable!=null )
                {
                    editable.clear();
                }
            }
            else if( primaryCode==CodeLeft )
            {
                if( start>0 )
                {
                    edittext.setSelection(start - 1);
                }
            }
            else if( primaryCode==CodeRight )
            {
                if (start < edittext.length())
                {
                    edittext.setSelection(start + 1);
                }
            }
            else
            { 
                editable.insert(start, Character.toString((char) primaryCode));
            }
        }
        @Override public void onPress(int arg0)
        {
        }

        @Override public void onRelease(int primaryCode)
        {
        }

        @Override public void onText(CharSequence text)
        {
        }

        @Override public void swipeDown()
        {
        }

        @Override public void swipeLeft()
        {
        }

        @Override public void swipeRight()
        {
        }

        @Override public void swipeUp()
        {
    }
    
    };

  
    public HexadecimalKbd(Activity host, int viewid, int layoutid)
    {
        mHostActivity= host;
        mKeyboardView= (KeyboardView)mHostActivity.findViewById(viewid);
        mKeyboardView.setKeyboard(new Keyboard(mHostActivity, layoutid));
        mKeyboardView.setPreviewEnabled(false); 
        mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
        mHostActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public boolean isCustomKeyboardVisible()
    {
        return (mKeyboardView.getVisibility()==View.VISIBLE);
    }

    public void showCustomKeyboard( View v )
    {
        mKeyboardView.setVisibility(View.VISIBLE);
        mKeyboardView.setEnabled(true);
        if( v!=null )
        {
            ((InputMethodManager)mHostActivity.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public void hideCustomKeyboard()
    {
        mKeyboardView.setVisibility(View.GONE);
        mKeyboardView.setEnabled(false);
    }

    public void registerEditText(int resid)
    {
        EditText edittext= (EditText)mHostActivity.findViewById(resid);
        edittext.setOnFocusChangeListener(new OnFocusChangeListener()
        {
            @Override public void onFocusChange(View v, boolean hasFocus)
            {
                if(bFirstIteration)
                {
                    if( hasFocus )
                    {
                        showCustomKeyboard(v);
                    }
                    else 
                    {
                        hideCustomKeyboard();
                    }
                }
                else
                {
                    hideCustomKeyboard();
                    bFirstIteration=true;
                }
            }
        });

        edittext.setOnClickListener(new OnClickListener()
        {
            @Override public void onClick(View v) 
            {
                showCustomKeyboard(v);
            }
        });

        edittext.setOnTouchListener(new OnTouchListener()
        {
            @Override public boolean onTouch(View v, MotionEvent event)
            {
                EditText edittext = (EditText) v;
                int inType = edittext.getInputType();
                edittext.setInputType(InputType.TYPE_NULL);
                edittext.onTouchEvent(event);
                edittext.setInputType(inType);
                return true;
            }
        });
        edittext.setInputType(edittext.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    }

}
