from django.shortcuts import render, redirect
from django.contrib.auth.forms import UserCreationForm
from django.contrib import messages

def register(request):
    if request.method == 'POST':
        user = UserCreationForm(request.POST)
        if user.is_valid():
            user.save()
            username = user.cleaned_data.get('username')
            messages.success(request, f'Account successfully created for {username}!')
            return redirect('amazon-home')
    else:
        form = UserCreationForm()
    return render(request, 'user/register.html', {'form': form})
