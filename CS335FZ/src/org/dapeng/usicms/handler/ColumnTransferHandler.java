/*******************************************************************************
 * Copyright (C) 2021 Dapeng Dong
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 ******************************************************************************/
package org.dapeng.usicms.handler;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

public class ColumnTransferHandler extends TransferHandler {
	private static final long serialVersionUID = 1L;
	private int[] indices = null;
	private int addIdx = -1;
	private int addCount = 0;

	protected String exportElement(JComponent jComponent) {
		JList list = (JList) jComponent;
		indices = list.getSelectedIndices();
		List values = list.getSelectedValuesList();

		StringBuffer buff = new StringBuffer();

		for (Object val : values) {
			buff.append(val == null ? "" : val.toString());
			buff.append("\n");
		}
		buff.deleteCharAt(buff.length() - 1);

		return buff.toString();
	}

	protected void importElement(JComponent jComponent, String str) {
		JList target = (JList) jComponent;
		DefaultListModel listModel = (DefaultListModel) target.getModel();
		int idx = target.getSelectedIndex();
		if (indices != null && idx >= indices[0] - 1 && idx <= indices[indices.length - 1]) {
			indices = null;
			return;
		}

		int max = listModel.getSize();
		if (idx < 0) {
			idx = max;
		} else {
			idx++;
			if (idx > max) {
				idx = max;
			}
		}
		addIdx = idx;

		ProjectLevelConfigs.UpdateUserStoryStatus(str, target.getName());

		String[] values = str.split("\n");
		addCount = values.length;
		for (int i = 0; i < values.length; i++) {
			listModel.add(idx++, values[i]);
		}

	}

	protected void cleanup(JComponent c, boolean remove) {
		if (remove && indices != null) {
			JList source = (JList) c;
			DefaultListModel model = (DefaultListModel) source.getModel();
			if (addCount > 0) {
				for (int i = 0; i < indices.length; i++) {
					if (indices[i] > addIdx) {
						indices[i] += addCount;
					}
				}
			}
			for (int i = indices.length - 1; i >= 0; i--) {
				model.remove(indices[i]);
			}
		}
		indices = null;
		addCount = 0;
		addIdx = -1;
	}

	protected Transferable createTransferable(JComponent c) {
		return new StringSelection(exportElement(c));
	}

	public int getSourceActions(JComponent c) {
		return COPY_OR_MOVE;
	}

	public boolean importData(JComponent jComponent, Transferable transferable) {
		if (canImport(jComponent, transferable.getTransferDataFlavors())) {
			try {
				String str = (String) transferable.getTransferData(DataFlavor.stringFlavor);
				importElement(jComponent, str);
				return true;
			} catch (UnsupportedFlavorException ufe) {
			} catch (IOException ioe) {
			}
		}

		return false;
	}

	protected void exportDone(JComponent jComponent, Transferable data, int action) {
		cleanup(jComponent, action == MOVE);
	}

	public boolean canImport(JComponent jComponent, DataFlavor[] flavors) {
		for (int i = 0; i < flavors.length; i++) {
			if (DataFlavor.stringFlavor.equals(flavors[i])) {
				return true;
			}
		}
		return false;
	}
}
