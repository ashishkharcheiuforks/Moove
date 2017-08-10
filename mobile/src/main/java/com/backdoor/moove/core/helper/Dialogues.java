package com.backdoor.moove.core.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.TextView;

import com.backdoor.moove.R;
import com.backdoor.moove.core.consts.Constants;
import com.backdoor.moove.core.consts.LED;
import com.backdoor.moove.core.consts.Language;
import com.backdoor.moove.core.consts.Prefs;
import com.backdoor.moove.core.file_explorer.FileExplorerActivity;
import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;

/**
 * Copyright 2015 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class Dialogues {

    /**
     * AlertDialog for selecting application screen orientation.
     *
     * @param context  application context.
     * @param listener listener for Dialog.
     */
    public static void imageDialog(final Activity context, @NonNull DialogInterface.OnDismissListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(context.getString(R.string.background_image));
        String[] types = new String[]{context.getString(R.string.none),
                context.getString(R.string.default_string),
                context.getString(R.string.select_image)};

        SharedPrefs prefs = new SharedPrefs(context);

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_single_choice, types);

        String image = prefs.loadPrefs(Prefs.REMINDER_IMAGE);
        int selection;
        if (image.matches(Constants.NONE)) {
            selection = 0;
        } else if (image.matches(Constants.DEFAULT)) {
            selection = 1;
        } else {
            selection = 2;
        }

        builder.setSingleChoiceItems(adapter, selection, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which != -1) {
                    dialog.dismiss();
                    SharedPrefs prefs = new SharedPrefs(context);
                    if (which == 0) {
                        prefs.savePrefs(Prefs.REMINDER_IMAGE, Constants.NONE);
                    } else if (which == 1) {
                        prefs.savePrefs(Prefs.REMINDER_IMAGE, Constants.DEFAULT);
                    } else if (which == 2) {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        if (Module.isKitkat()) {
                            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("image/*");
                        }
                        Intent chooser = Intent.createChooser(intent, context.getString(R.string.select_image));
                        context.startActivityForResult(chooser, Constants.ACTION_REQUEST_GALLERY);
                    }
                }
            }
        });
        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                SharedPrefs prefs = new SharedPrefs(context);
                if (which == 0) {
                    prefs.savePrefs(Prefs.REMINDER_IMAGE, Constants.NONE);
                } else if (which == 1) {
                    prefs.savePrefs(Prefs.REMINDER_IMAGE, Constants.DEFAULT);
                } else if (which == 2) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    if (Module.isKitkat()) {
                        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("image/*");
                    }
                    Intent chooser = Intent.createChooser(intent, context.getString(R.string.select_image));
                    context.startActivityForResult(chooser, Constants.ACTION_REQUEST_GALLERY);
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(listener);
        dialog.show();
    }

    /**
     * Create and AlertDialog with customizable seekbar.
     *
     * @param context  Application context.
     * @param max      seekbar maximum.
     * @param prefs    Preference key for saving result.
     * @param title    title for Dialog.
     * @param listener Dialog action listener.
     */
    public static void dialogWithSeek(final Context context, int max, final String prefs, String title,
                                      DialogInterface.OnDismissListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(title);
        final SharedPrefs sharedPrefs = new SharedPrefs(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_seekbar, null);
        final TextView textView = layout.findViewById(R.id.seekValue);
        SeekBar seekBar = layout.findViewById(R.id.dialogSeek);
        seekBar.setMax(max);
        int progress = sharedPrefs.loadInt(prefs);
        seekBar.setProgress(progress);
        textView.setText(String.valueOf(progress));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText(String.valueOf(progress));
                sharedPrefs.saveInt(prefs, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        builder.setView(layout);
        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(listener);
        dialog.show();
    }

    /**
     * AlertDialog for selecting type of melody - system or custom file.
     *
     * @param context     application context.
     * @param prefsToSave Preference key to save result.
     */
    public static void melodyType(final Activity context, final String prefsToSave,
                                  final int requestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(context.getString(R.string.melody));
        String[] types = new String[]{context.getString(R.string.default_string),
                context.getString(R.string.select_file)};

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_single_choice, types);

        SharedPrefs prefs = new SharedPrefs(context);
        int position;
        if (!prefs.loadBoolean(prefsToSave)) {
            position = 0;
        } else {
            position = 1;
        }

        builder.setSingleChoiceItems(adapter, position, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which != -1) {
                    SharedPrefs prefs = new SharedPrefs(context);
                    if (which == 0) {
                        prefs.saveBoolean(prefsToSave, false);
                    } else {
                        prefs.saveBoolean(prefsToSave, true);
                        dialog.dismiss();
                        context.startActivityForResult(new Intent(context, FileExplorerActivity.class), requestCode);
                    }
                }
            }
        });
        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPrefs prefs = new SharedPrefs(context);
                if (!prefs.loadBoolean(prefsToSave)) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * AlertDialog for selecting LED indicator color for events.
     *
     * @param context application context.
     */
    public static void ledColor(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle(context.getString(R.string.led_color));

        String[] colors = new String[LED.NUM_OF_LEDS];
        for (int i = 0; i < LED.NUM_OF_LEDS; i++) {
            colors[i] = LED.getTitle(context, i);
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_single_choice, colors);

        SharedPrefs prefs = new SharedPrefs(context);
        int position = prefs.loadInt(Prefs.LED_COLOR);

        builder.setSingleChoiceItems(adapter, position, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which != -1) {
                    SharedPrefs prefs = new SharedPrefs(context);
                    prefs.saveInt(Prefs.LED_COLOR, which);
                }
            }
        });
        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * AlertDialog for selecting language for voice notifications (text to speech).
     *
     * @param context     application context.
     * @param prefsToSave Preference key for results saving.
     */
    public static void ttsLocale(final Context context, final String prefsToSave) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle(R.string.language);
        ArrayList<String> names = new ArrayList<>();
        names.add(context.getString(R.string.english));
        names.add(context.getString(R.string.french));
        names.add(context.getString(R.string.german));
        names.add(context.getString(R.string.italian));
        names.add(context.getString(R.string.japanese));
        names.add(context.getString(R.string.korean));
        names.add(context.getString(R.string.polish));
        names.add(context.getString(R.string.russian));
        names.add(context.getString(R.string.spanish));

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_single_choice, names);

        SharedPrefs prefs = new SharedPrefs(context);
        int position = 1;
        String locale = prefs.loadPrefs(prefsToSave);
        if (locale.matches(Language.ENGLISH)) position = 0;
        if (locale.matches(Language.FRENCH)) position = 1;
        if (locale.matches(Language.GERMAN)) position = 2;
        if (locale.matches(Language.ITALIAN)) position = 3;
        if (locale.matches(Language.JAPANESE)) position = 4;
        if (locale.matches(Language.KOREAN)) position = 5;
        if (locale.matches(Language.POLISH)) position = 6;
        if (locale.matches(Language.RUSSIAN)) position = 7;
        if (locale.matches(Language.SPANISH)) position = 8;

        builder.setSingleChoiceItems(adapter, position, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which != -1) {
                    SharedPrefs prefs = new SharedPrefs(context);
                    String locale = Language.ENGLISH;
                    if (which == 0) locale = Language.ENGLISH;
                    if (which == 1) locale = Language.FRENCH;
                    if (which == 2) locale = Language.GERMAN;
                    if (which == 3) locale = Language.ITALIAN;
                    if (which == 4) locale = Language.JAPANESE;
                    if (which == 5) locale = Language.KOREAN;
                    if (which == 6) locale = Language.POLISH;
                    if (which == 7) locale = Language.RUSSIAN;
                    if (which == 8) locale = Language.SPANISH;
                    prefs.savePrefs(prefsToSave, locale);
                }
            }
        });
        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * AlertDialog for selecting map type.
     *
     * @param context application context.
     */
    public static void mapType(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(context.getString(R.string.map_type));

        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.map_types,
                android.R.layout.simple_list_item_single_choice);

        SharedPrefs prefs = new SharedPrefs(context);
        int type = prefs.loadInt(Prefs.MAP_TYPE);
        int position;
        if (type == GoogleMap.MAP_TYPE_NORMAL) {
            position = 0;
        } else if (type == GoogleMap.MAP_TYPE_SATELLITE) {
            position = 1;
        } else if (type == GoogleMap.MAP_TYPE_HYBRID) {
            position = 2;
        } else if (type == GoogleMap.MAP_TYPE_TERRAIN) {
            position = 3;
        } else {
            position = 0;
        }

        builder.setSingleChoiceItems(adapter, position, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which != -1) {
                    SharedPrefs prefs = new SharedPrefs(context);
                    if (which == 0) {
                        prefs.saveInt(Prefs.MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL);
                    } else if (which == 1) {
                        prefs.saveInt(Prefs.MAP_TYPE, GoogleMap.MAP_TYPE_SATELLITE);
                    } else if (which == 2) {
                        prefs.saveInt(Prefs.MAP_TYPE, GoogleMap.MAP_TYPE_HYBRID);
                    } else {
                        prefs.saveInt(Prefs.MAP_TYPE, GoogleMap.MAP_TYPE_TERRAIN);
                    }
                }
            }
        });
        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
