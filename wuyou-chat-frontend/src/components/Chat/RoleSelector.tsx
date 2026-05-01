import { useState } from 'react';
import { ROLE_TYPES } from '../../types';

interface Props {
  currentRole: string;
  onChange: (roleType: string) => void;
}

export function RoleSelector({ currentRole, onChange }: Props) {
  const [open, setOpen] = useState(false);

  const current = ROLE_TYPES.find(r => r.value === currentRole) || ROLE_TYPES[0];

  return (
    <div className="role-selector" onMouseLeave={() => setOpen(false)}>
      <button className="role-current" onClick={() => setOpen(!open)}>
        {current.displayName} ▾
      </button>
      {open && (
        <div className="role-dropdown">
          {ROLE_TYPES.map(role => (
            <button
              key={role.value}
              className={`role-option ${role.value === currentRole ? 'selected' : ''}`}
              onClick={() => {
                onChange(role.value);
                setOpen(false);
              }}
            >
              {role.displayName}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
